package nav

import cats.effect.Effect
import fs2.Stream
import java.nio.ByteBuffer

abstract class Client[F[_]: Effect] {
  def token: F[Token]
  def streamSubscription(id: SubscriptionId): F[Stream[F, String]]
}
