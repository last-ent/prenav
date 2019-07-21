package nav

import cats.effect.Effect
import fs2.concurrent.Queue

final case class Token(value: String) extends AnyVal

final case class SubscriptionId(value: String) extends AnyVal

final case class Channels[F[_]: Effect, A](input: SubscriptionId, output: Queue[F, A])
