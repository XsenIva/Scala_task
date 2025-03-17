package models

case class User(
  id: Long,
  username: String,
  email: String,
  passwordHash: String,
  pixelCount: Int
)

case class Team(
  id: Long,
  name: String,
  member_id: List[Int] = List.empty
)

case class Leaderboard(
  userId: Long,
  username: String,
  pixelCount: Int
)