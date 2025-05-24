package repository

import cats.effect.IO

import doobie._
import doobie.implicits._
import cats.implicits._
import models.Game

import java.time.LocalDateTime
import java.sql.Timestamp
import doobie.implicits.javatime._ 

trait GameRepository{
  def findAll(): IO[List[Game]]
  def findById(id: Option[Long]): IO[Option[Game]]
  def create(game: Game):  IO[Game] 
  def delete(id: Option[Long]): IO[Int]
}

class GameRepositoryImpl(xa: Transactor[IO]) extends GameRepository{

 override def findAll(): IO[List[Game]] = 
     sql"""
      SELECT id, status_game, creation_date 
      FROM games
      """.query[(Option[Long], String, LocalDateTime)]
      .map{case (id, status_game, creation_date) =>
      Game(id = id, status = status_game, creationtime = creation_date)}
      .to[List]
      .transact(xa)


  override def findById(id: Option[Long]): IO[Option[Game]] = {
    id match {
      case Some(gameId) =>
        sql"""
          SELECT id, status_game, creation_date 
          FROM games
          WHERE id = $gameId
        """.query[(Option[Long], String, LocalDateTime)]
           .map{case (id, status_game, creation_date) =>
                Game(id = id, status = status_game, creationtime = creation_date)}
           .option 
           .transact(xa)
      case None => IO.pure(None)
    }
  }

       
  override def create(game: Game): IO[Game] = 
    sql"""
      INSERT INTO games (status_game, creation_date)
      VALUES (${game.status}, ${game.creationtime})
    """.update
       .withUniqueGeneratedKeys[Long]("id")
       .map(id => game.copy(id = Some(id)))
       .transact(xa)


  override def delete(id: Option[Long]): IO[Int] = {
    id match {
      case Some(gameId) =>
        sql"""
          DELETE FROM games 
          WHERE id = $gameId
        """.update
           .run
           .transact(xa)
      case None => IO.pure(0)
    }
  }
}

