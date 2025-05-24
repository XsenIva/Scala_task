package service

import java.time.LocalDateTime
import models.Move

case class GameField(
  width: Int, 
  height: Int, 
  defaultColor: String, 
  pixels: List[Move]
)

object GameField {
  val DefaultWidth: Int = 100
  val DefaultHeight: Int = 100
  val DefaultColor: String = "#FFFFFF"

  def defaultField(): GameField = {
    val pixels = (0 until DefaultWidth).flatMap(x =>
      (0 until DefaultHeight).map(y => 
        Move(
          id = None,
          gameid = 0L, 
          playerid = 0L,
          x = x,
          y = y,
          color = DefaultColor,
          creationtime = LocalDateTime.now()
        )
      )
    ).toList
    GameField(DefaultWidth, DefaultHeight, DefaultColor, pixels)
  }

  def fromMoves(moves: List[Move]): GameField = {
    val pixels = if (moves.isEmpty) {
      defaultField().pixels
    } else {
      moves
    }
    GameField(DefaultWidth, DefaultHeight, DefaultColor, pixels)
  }
}