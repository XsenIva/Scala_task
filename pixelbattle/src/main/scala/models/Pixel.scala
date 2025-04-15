package models
import java.time.LocalDateTime

case class Move(
  id: Option[Long],
  gameid: Long,
  playerid: Long,
  x: Int,
  y: Int,
  color: String,
  creationtime: LocalDateTime
)

case class Game(
  id: Option[Long],
  status: String,
  creationtime: LocalDateTime
)


