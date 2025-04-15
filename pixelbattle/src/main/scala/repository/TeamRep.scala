package repository


import cats.effect.IO

import doobie._
import doobie.implicits._
import cats.implicits._
import models.Team


trait TeamRepository{
  def findAll(): IO[List[Team]]
  // def findById(id: Long): IO[Option[User]]
  def create(team: Team): IO[Team]
  def delete(id: Option[Long]): IO[Int]
}


class TeamRepositoryImpl(xa: Transactor[IO]) extends TeamRepository{

  override def findAll(): IO[List[Team]] = 
    sql"""
      SELECT id, team_name, player_id 
      FROM teams
    """.query[(Option[Long], String, Long)]
     .map{case (id,  team_name, player_id ) =>
     Team(id = id , name = team_name, lead = player_id)}
     .to[List].transact(xa)

   
  override def create(team: Team): IO[Team] = 
    sql"""
      INSERT INTO teams (team_name, player_id)
      VALUES (${team.name}, ${team.lead})
    """.update
       .withUniqueGeneratedKeys[Option[Long]]("id")
       .map(id => team.copy(id = id))
       .transact(xa)


  override def delete(id: Option[Long]): IO[Int] = 
    sql"""
      DELETE FROM teams WHERE id = $id
    """.update.run.transact(xa)
}