package models

case class Canvas(
  id: Long,
  width: Int,
  height: Int,
  pixels: List[Pixel] = List.empty
)

case class Leaderboard(
  userId: Long,
  username: String,
  pixelCount: Int
)

case class ColorPalette(
  id: Long,
  color: String
)