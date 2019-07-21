package nav

import fs2.Stream
import cats.effect.Effect
import cats.effect.ContextShift
import fs2.concurrent.Queue
import cats.implicits._

sealed abstract class Navika[F[_]: Effect, A] {
  def readChunks: Stream[F, Unit]
}

class Navika1[F[_]: Effect, A](
    client: Client,
    channels: Channels[F, String],
    cs: ContextShift[F]
) extends Navika[F, A] {
  implicit val ctxShft: ContextShift[F] = cs

  def readChunks: Stream[F, Unit] =
    for {
      chunks <- Stream.eval(client.streamSubscription(channels.input))
      chunk <- chunks
      _ <- Stream.eval(channels.output.enqueue1(chunk))
    } yield ()

}

object Navika {
  def apply[F[_]: Effect](
      client: Client,
      channels: Channels[F, String],
      ctxShft: ContextShift[F]
  ) = new Navika1[F, String](client, channels, ctxShft)
}
