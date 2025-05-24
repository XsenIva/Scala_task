package repository


import cats.effect.IO

import doobie._
import doobie.implicits._
import cats.implicits._
import models.Move

import java.time.LocalDateTime
import java.sql.Timestamp
import doobie.implicits.javatime._ 

trait MoveRepository{
  def findAll(): IO[List[Move]]
  def findById(id: Option[Long]): IO[Option[Move]]
  def create(move: Move): IO[Move]
  def delete(id: Option[Long]): IO[Int]
}

class MoveRepositoryImpl(xa: Transactor[IO]) extends MoveRepository{

  override def findAll(): IO[List[Move]] = 
     sql"""
      SELECT id, game_id, player_id,
      x_coordinate, y_coordinate, color, move_time
      FROM moves
    """.query[(Option[Long], Long, Long, Int, Int, String, LocalDateTime)]
     .map{case (id, game_id, player_id,
                x_coordinate, y_coordinate,
                color, move_time) =>
     Move(id = id, gameid = game_id, playerid = player_id,
          x = x_coordinate, y = y_coordinate, color = color, creationtime = move_time)}
     .to[List].transact(xa)


  override def findById(id: Option[Long]): IO[Option[Move]] = {
    id match {
      case Some(moveId) =>
        sql"""
          SELECT id, game_id, player_id,
          x_coordinate, y_coordinate, color, move_time
          FROM moves
          WHERE id = $moveId
        """.query[(Option[Long], Long, Long, Int, Int, String, LocalDateTime)]
           .map{case (id, game_id, player_id, x_coordinate, y_coordinate, color, move_time) =>
             Move(id = id, gameid = game_id, playerid = player_id,
                  x = x_coordinate, y = y_coordinate, color = color, creationtime = move_time)}
           .option 
           .transact(xa)
      case None => IO.pure(None)
    }
  }

       
  override def create(move: Move): IO[Move] = 
    sql"""
      INSERT INTO moves (game_id, player_id,
      x_coordinate, y_coordinate, color, move_time)
      VALUES (${move.gameid}, ${move.playerid}, 
              ${move.x}, ${move.y},
              ${move.color}, ${move.creationtime})
    """.update
       .withUniqueGeneratedKeys[Long]("id")
       .map(id => move.copy(id = Some(id)))
       .transact(xa)


  override def delete(id: Option[Long]): IO[Int] = {
    id match {
      case Some(moveId) =>
        sql"""
          DELETE FROM moves 
          WHERE id = $moveId
        """.update
           .run
           .transact(xa)
      case None => IO.pure(0)
    }
  }
}
