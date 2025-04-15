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

case class Session(
  id: Option[Long],
  playerid: Long,
  token: String,
  creationtime: LocalDateTime,
  expirationtime: LocalDateTime

)
