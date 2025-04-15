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
  // def findById(id: Long): IO[Option[User]]
  def create(game: Game):  IO[Game] 
  // def update(game: Game): IO[Int]
  def delete(id: Option[Long]): IO[Int]
}

class GameRepositoryImpl(xa: Transactor[IO]) extends GameRepository{
 override def findAll(): IO[List[Game]] = 
     sql"""
      SELECT id, status_game, creation_time 
      FROM games
      """.query[(Option[Long], String, LocalDateTime)]
      .map{case (id, status_game, creation_time) =>
      Game(id = id, status = status_game, creationtime = creation_time)}
      .to[List]
      .transact(xa)

       
  override def create(game: Game): IO[Game] = 
    sql"""
      INSERT INTO games (status_game, creation_time)
      VALUES (${game.status}, ${game.creationtime})
    """.update
       .withUniqueGeneratedKeys[Option[Long]]("id")
       .map(id => game.copy(id = id))
       .transact(xa)


  override def delete(id: Option[Long]): IO[Int] = 
    sql"""
      DELETE FROM games WHERE id = $id
    """.update.run.transact(xa)

}

