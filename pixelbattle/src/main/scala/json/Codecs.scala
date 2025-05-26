package json

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._
import service.GameField
import models.Move

object Codecs {
  implicit val gameFieldEncoder: Encoder[GameField] = deriveEncoder[GameField]
  implicit val gameFieldDecoder: Decoder[GameField] = deriveDecoder[GameField]
  
  implicit val moveEncoder: Encoder[Move] = deriveEncoder[Move]
  implicit val moveDecoder: Decoder[Move] = deriveDecoder[Move]
} 