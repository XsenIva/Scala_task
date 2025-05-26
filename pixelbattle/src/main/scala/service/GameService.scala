package service

import repository.{GameRepository, TeamRepository, MoveRepository}
import models.{Game, Team, Move}
import java.time.LocalDateTime
import cats.effect.unsafe.implicits.global

trait GameService {
  def createGame(): GameField
  def addPlayer(playerid: Long): GameField
  def makeMove(gameid: Long, playerid: Long, x: Int, y: Int, color: String): Either[String, Move]
  def canMakeMove(gameId: Long, playerId: Long, x: Int, y: Int): Either[String, Unit]
  def getUserScore(userId: Long): Int
  def getCurrentGameId: Option[Long]
  def getCurrentGame(): GameField
  def getGameById(id: Long): Option[GameField]
}


//  Создать игру = создать поле 
//  добавить игрока на поле  = создать команду из 1 игрока, которую привяжем к игре 
//  получить очки игрока = посчитать findById по из Мove
//  сделать ход - проверить таймер, добавить пиксель, обновить таймер


class GameServiceImpl(
  gameRepository: GameRepository,
  teamRepository: TeamRepository,
  moveRepository: MoveRepository

) extends GameService {

  private var currentGameId: Option[Long] = None

  override def getCurrentGameId: Option[Long] = currentGameId

  override def createGame(): GameField = {
    val field = GameField.defaultField()
    val game = Game(None, "active", LocalDateTime.now())
    val createdGame = gameRepository.create(game).unsafeRunSync()
    currentGameId = createdGame.id
    field
  }

  override def getCurrentGame(): GameField = {
    currentGameId match {
      case Some(id) =>
        val moves = moveRepository.findAll().unsafeRunSync()
          .filter(_.gameid == id)
        GameField.fromMoves(moves)
      case None =>
        GameField.defaultField()
    }
  }

  override def getGameById(id: Long): Option[GameField] = {
    try {
      gameRepository.findById(Some(id)).unsafeRunSync() match {
        case Some(_) =>
          val moves = moveRepository.findAll().unsafeRunSync()
            .filter(_.gameid == id)
          Some(GameField.fromMoves(moves))
        case None => None
      }
    } catch {
      case _: Exception => None
    }
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

  override def canMakeMove(gameId: Long, playerId: Long, x: Int, y: Int): Either[String, Unit] = {
    val moves = moveRepository.findAll().unsafeRunSync()
    val gameMoves = moves.filter(_.gameid == gameId)
    val playerMoves = gameMoves.filter(_.playerid == playerId)
    if (playerMoves.size >= 10) {
      Left("You have already made 10 moves")
    } else {
      Right(())
    }
  }
} 
