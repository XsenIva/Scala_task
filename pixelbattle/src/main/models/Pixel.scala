package models

case class Pixel(
  id: Long,
  x: Int,
  y: Int,
  color: String,
  userId: Long,
  timestamp: Long
)

case class PixelLog(
  id: Long,
  pixelId: Long,
  oldColor: String,
  newColor: String,
  userId: Long,
  timestamp: Long
)