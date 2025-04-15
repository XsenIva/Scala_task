package db.repository


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
  // def findById(id: Long): IO[Option[User]]
  def create(move: Move): IO[Int]
  def delete(id: Long): IO[Int]
}

class MoveRepositoryImpl(xa: Transactor[IO]) extends MoveRepository{

  override def findAll(): IO[List[Move]] = 
     sql"""
      SELECT id, game_id, player_id,
      x_coordinate, y_coordinate, color, move_time
      FROM moves
    """.query[(Long, Long,Long,Int, Int, String, LocalDateTime)]
     .map{case (id, game_id, player_id,
                x_coordinate, y_coordinate,
                color, move_time) =>
     Move(id = id, gameid = game_id, playerid = player_id,
          x = x_coordinate, y = y_coordinate, color = color , creationtime = move_time)}
     .to[List].transact(xa)

       
  override def create(move: Move): IO[Int] = 
    sql"""
      INSERT INTO moves (game_id, player_id,
      x_coordinate, y_coordinate, color, move_time)
      VALUES (${move.gameid}, ${move.playerid}, 
              ${move.x}, ${move.y},
              ${move.color}, ${move.creationtime})
    """.update.run.transact(xa)


  override def delete(id: Long): IO[Int] = 
    sql"""
      DELETE FROM moves WHERE id = $id
    """.update.run.transact(xa) 
}
