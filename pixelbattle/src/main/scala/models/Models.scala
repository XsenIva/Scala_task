package models

import java.time.LocalDateTime

case class User(
  id: Option[Long],
  name: String,
  email: String,
  passwordHash: String
)

case class Team(
  id: Option[Long],
  name: String,
  lead: Long
)

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