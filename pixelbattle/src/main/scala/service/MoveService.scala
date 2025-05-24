package service

import java.time.{LocalDateTime, Duration}
import repository.{MoveRepository, GameRepository, UserRepository}
import models.Move
import cats.effect.unsafe.implicits.global
import cats.effect.IO

enum Color(val hex: String) {
  case Red   extends Color("#FF0000")
  case Green extends Color("#00FF00")
  case Blue  extends Color("#0000FF")
  case White extends Color("#FFFFFF")
  case Black extends Color("#000000")
}

enum MoveError {
  case InvalidCoordinates
  case InvalidColor
  case TimeLimit
  case GameNotFound
  case PlayerNotFound
  case DatabaseError(message: String)
}

trait MoveService {
  def makeMove(gameid: Long, playerid: Long, x: Int, y: Int, color: String): Either[MoveError, Move]
  def getMove(x: Int, y: Int): Option[Move]
  def getField(): List[Move]
  def getLastMoveTime(playerid: Long): Option[LocalDateTime]
}

//  Сделать ход
//  Получить ходы

class MoveServiceImpl(
  moveRepository: MoveRepository,
  gameRepository: GameRepository,
  userRepository: UserRepository,
  timeBetweenMoves: Duration = Duration.ofSeconds(60)
) extends MoveService {

  private def validateCoordinates(x: Int, y: Int): Boolean = {
    x >= 0 && x < GameField.DefaultWidth && y >= 0 && y < GameField.DefaultHeight
  }

  private def validateColor(color: String): Boolean = {
    Color.values.exists(_.hex.equalsIgnoreCase(color))
  }

  private def validateTimeLimit(playerid: Long): Boolean = {
    getLastMoveTime(playerid) match {
      case Some(lastMoveTime) =>
        val now = LocalDateTime.now()
        val timeSinceLastMove = Duration.between(lastMoveTime, now)
        timeSinceLastMove.compareTo(timeBetweenMoves) >= 0
      case None => true
    }
  }

  override def makeMove(gameid: Long, playerid: Long, x: Int, y: Int, color: String): Either[MoveError, Move] = {
    if (!validateCoordinates(x, y)) {
      Left(MoveError.InvalidCoordinates)
    } else if (!validateColor(color)) {
      Left(MoveError.InvalidColor)
    } else {
      try {
        val result = (for {
          gameExists <- gameRepository.findById(Some(gameid))
          playerExists <- userRepository.findById(Some(playerid))
          _ <- IO {
            if (gameExists.isEmpty) throw new Exception("Game not found")
            if (playerExists.isEmpty) throw new Exception("Player not found")
            if (!validateTimeLimit(playerid)) throw new Exception("Time limit not met")
          }
          move = Move(
            id = None,
            gameid = gameid,
            playerid = playerid,
            x = x,
            y = y,
            color = color,
            creationtime = LocalDateTime.now()
          )
          createdMove <- moveRepository.create(move)
        } yield createdMove).unsafeRunSync()
        
        Right(result)
      } catch {
        case e: Exception => 
          val error = e.getMessage match {
            case "Game not found" => MoveError.GameNotFound
            case "Player not found" => MoveError.PlayerNotFound
            case "Time limit not met" => MoveError.TimeLimit
            case msg => MoveError.DatabaseError(msg)
          }
          Left(error)
      }
    }
  }

  override def getMove(x: Int, y: Int): Option[Move] = {
    try {
      moveRepository.findAll().unsafeRunSync()
        .find(m => m.x == x && m.y == y)
    } catch {
      case _: Exception => None
    }
  }

  override def getField(): List[Move] = {
    try {
      val moves = moveRepository.findAll().unsafeRunSync()
      GameField.fromMoves(moves).pixels
    } catch {
      case _: Exception => GameField.defaultField().pixels
    }
  }

  override def getLastMoveTime(playerid: Long): Option[LocalDateTime] = {
    try {
      moveRepository.findAll().unsafeRunSync()
        .filter(_.playerid == playerid)
        .map(_.creationtime)
        .maxOption
    } catch {
      case _: Exception => None
    }
  }
}