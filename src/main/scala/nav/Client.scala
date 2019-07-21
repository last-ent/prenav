package nav

import cats.effect.Effect
import fs2.Stream

trait Client {
  def token[F[_]: Effect]: F[Token]
  def streamSubscription[F[_]: Effect](id: SubscriptionId): F[Stream[F, String]]
}
