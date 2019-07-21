package example

import cats.effect.{IO, IOApp, Effect}
import scala.concurrent.ExecutionContext
import cats.effect.ExitCode
import fs2.Stream
import fs2.concurrent.Queue
import nav.Client
import nav.Token
import nav.SubscriptionId
import nav.Channels
import nav.Navika
import cats.implicits._

object FakeClient extends Client {
  override def token[F[_]: Effect]: F[Token] =
    Effect[F].pure(new Token("tok"))

  override def streamSubscription[F[_]: Effect](id: SubscriptionId): F[Stream[F, String]] =
  Effect[F].pure(Stream(s"Hello for $id").repeat)
}

object Hello extends IOApp {
  implicit val ctxShft = IO.contextShift(ExecutionContext.Implicits.global)

  def receiveMessages(queue: Queue[IO, String]) =
    for {
      _ <- queue.dequeue.evalMap(msg => IO.delay(println(s"Received: $msg")))
    } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    val stream =
      for {
        q <- Stream.eval(Queue.bounded[IO, String](100))
        channels = new Channels[IO, String](SubscriptionId("sub-id-1"), q)
        navika = Navika[IO](FakeClient, channels, ctxShft)
        _ <- startF(navika, q).drain
      } yield ()

    stream.compile.drain.as(ExitCode.Success)
  }

  def startF(navika: Navika[IO, String], queue: Queue[IO, String]): Stream[IO, Unit] =
    Stream(
      navika.readChunks,
      receiveMessages(queue)
    ).parJoin(2)
}
