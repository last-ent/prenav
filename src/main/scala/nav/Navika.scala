package nav

import fs2.Stream
import cats.effect.Effect
import cats.effect.ContextShift
import fs2.concurrent.Queue
import cats.implicits._
import io.circe.Json
import java.nio.ByteBuffer
import io.circe.fs2._
import io.circe._
import io.circe.parser._

sealed abstract class Navika[F[_]: Effect] {
  def readChunks: Stream[F, Unit]
}

class Navika1[F[_]: Effect](
    client: Client[F],
    channels: Channels[F],
    cs: ContextShift[F]
) extends Navika[F] {
  implicit val ctxShft: ContextShift[F] = cs

  override def readChunks: Stream[F, Unit] =
    for {
      chunks <- Stream.eval(client.streamSubscription(channels.input))
      chunk <- chunks.through(stringStreamParser)
      _ <- Stream.eval(channels.output.enqueue1(chunk))
    } yield ()
}

object Navika {
  def apply[F[_]: Effect](
      client: Client[F],
      channels: Channels[F],
      ctxShft: ContextShift[F]
  ) = new Navika1[F](client, channels, ctxShft)
}
