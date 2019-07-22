package example

import cats.effect.{IO, IOApp, Effect}
import scala.concurrent.ExecutionContext
import cats.effect.ExitCode
import fs2.Stream
import fs2.concurrent.Queue
import nav.{Client, Token, SubscriptionId, Channels, Navika}
import cats.implicits._
import io.circe._
import io.circe.parser._
import java.nio.ByteBuffer

class FakeClient[F[_]: Effect] extends Client[F] {
  val rawStream = Stream(
    """{"cursor":{"partition":"5","offset":"543","event_type":"order.ORDER_RECEIVED","cursor_token":"b75c3102-98a4-4385-a5fd-b96f1d7872f2"},"events":[{"metadata":{"occurred_at":"1996-10-15T16:39:57+07:00","eid":"1f5a76d8-db49-4144-ace7-e683e8ff4ba4","event_type":"aruha-test-hila","partition":"5","received_at":"2016-09-30T09:19:00.525Z","flow_id":"blahbloh"},"data_op":"C","data":{"order_number":"abc","id":"111"},"data_type":"blah"}],"info":{"debug":"Stream started"}}""",
    """{"cursor":{"partition":"5","offset":"544","event_type":"order.ORDER_RECEIVED","cursor_token":"a28568a9-1ca0-4d9f-b519-dd6dd4b7a610"},"events":[{"metadata":{"occurred_at":"1996-10-15T16:39:57+07:00","eid":"1f5a76d8-db49-4144-ace7-e683e8ff4ba4","event_type":"aruha-test-hila","partition":"5","received_at":"2016-09-30T09:19:00.741Z","flow_id":"blahbloh"},"data_op":"C","data":{"order_number":"abc","id":"111"},"data_type":"blah"}]}""",
    """{"cursor":{"partition":"5","offset":"545","event_type":"order.ORDER_RECEIVED","cursor_token":"a241c147-c186-49ad-a96e-f1e8566de738"},"events":[{"metadata":{"occurred_at":"1996-10-15T16:39:57+07:00","eid":"1f5a76d8-db49-4144-ace7-e683e8ff4ba4","event_type":"aruha-test-hila","partition":"5","received_at":"2016-09-30T09:19:00.741Z","flow_id":"blahbloh"},"data_op":"C","data":{"order_number":"abc","id":"111"},"data_type":"blah"}]}""",
    """{"cursor":{"partition":"0","offset":"545","event_type":"order.ORDER_RECEIVED","cursor_token":"bf6ee7a9-0fe5-4946-b6d6-30895baf0599"}}""",
    """{"cursor":{"partition":"1","offset":"545","event_type":"order.ORDER_RECEIVED","cursor_token":"9ed8058a-95be-4611-a33d-f862d6dc4af5"}}"""
  )
  override def token: F[Token] =
    Effect[F].pure(new Token("tok"))

  override def streamSubscription(id: SubscriptionId): F[Stream[F, String]] = {
    Effect[F].pure(rawStream)
  }
}

object Hello extends IOApp {
  implicit val ctxShft = IO.contextShift(ExecutionContext.Implicits.global)

  def receiveMessages(queue: Queue[IO, Json]) =
    for {
      _ <- queue.dequeue.evalMap(msg => IO.delay(println(s"Received: $msg")))
    } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    val stream =
      for {
        q <- Stream.eval(Queue.bounded[IO, Json](100))
        channels = new Channels[IO](SubscriptionId("sub-id-1"), q)
        navika = Navika[IO](new FakeClient[IO](), channels, ctxShft)
        _ <- startF(navika, q).drain
      } yield ()

    stream.compile.drain.as(ExitCode.Success)
  }

  def startF(navika: Navika[IO], queue: Queue[IO, Json]): Stream[IO, Unit] =
    Stream(
      navika.readChunks,
      receiveMessages(queue)
    ).parJoin(2)
}
