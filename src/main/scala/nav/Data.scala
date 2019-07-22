package nav

import cats.effect.Effect
import fs2.concurrent.Queue
import io.circe.Json

final case class Token(value: String) extends AnyVal

final case class SubscriptionId(value: String) extends AnyVal

final case class Channels[F[_]: Effect](input: SubscriptionId, output: Queue[F, Json])
