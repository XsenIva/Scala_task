package service

import repository.{GameRepository, TeamRepository, MoveRepository}
import models.{Game, Team, Move}
import java.time.LocalDateTime
import cats.effect.unsafe.implicits.global

trait GameService {
  def createGame(width: Int, height: Int): GameField
  def addPlayer(playerid: Long): GameField
  def makeMove(gameid: Long, playerid: Long, x: Int, y: Int, color: String): Either[String, Move]
  def getUserScore(userId: Long): Int
}


//  Создать игру = создать поле 
//  добавить игрока на поле  = создать команду из 1 игрока, которую привяжем к игре 
//  получить очки игрока = посчитать findById по из Мove
//  сделать ход - проверить таймер, добавить пиксель, обновить таймер


class GameServiceImpl(
  gameRepository: GameRepository,
  teamRepository: TeamRepository,
  moveRepository: MoveRepository
  // timeControlService: TimeControlService
) extends GameService {

  override def createGame(width: Int, height: Int): GameField = {
    val field = GameField.defaultField()
    val game = Game(None, "active", LocalDateTime.now())
    gameRepository.create(game).unsafeRunSync()
    field
  }

  override def addPlayer(playerid: Long): GameField = {
    val teamName = s"Team_${playerid}"
    val team = Team(None, teamName, playerid)
    val createdTeam = teamRepository.create(team).unsafeRunSync()
    
    val moves = moveRepository.findAll().unsafeRunSync()
    GameField.fromMoves(moves)
  }

  override def makeMove(gameid: Long, playerid: Long, x: Int, y: Int, color: String): Either[String, Move] = {
    if (x < 0 || x >= GameField.DefaultWidth || y < 0 || y >= GameField.DefaultHeight) {
      Left("Invalid coordinates")
    } else {
      val move = Move(
        id = None,
        gameid = gameid,
        playerid = playerid,
        x = x,
        y = y,
        color = color,
        creationtime = LocalDateTime.now()
      )
      
      try {
        Right(moveRepository.create(move).unsafeRunSync())
      } catch {
        case e: Exception => Left(s"Failed to make move: ${e.getMessage}")
      }
    }
  }

  override def getUserScore(userId: Long): Int = {
    val moves = moveRepository.findAll().unsafeRunSync()
    val userMoves = moves.count(_.playerid == userId)
    userMoves * 5 
  }
} 
