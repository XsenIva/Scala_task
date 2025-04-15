package models

case class GameField(width: Int, height: Int, defaultColor: String)

object GameField {
  val DefaultWidth: Int = 500
  val DefaultHeight: Int = 500
  val DefaultColor: String = "#FFFFFF"

  def defaultField(): GameField =
    GameField(DefaultWidth, DefaultHeight, DefaultColor)
}