package repository


import cats.effect.IO

import doobie._
import doobie.implicits._
import cats.implicits._
import models.Team


trait TeamRepository{
  def findAll(): IO[List[Team]]
  def findById(id: Option[Long]): IO[Option[Team]]
  def create(team: Team): IO[Team]
  def delete(id: Option[Long]): IO[Int]
}

class TeamRepositoryImpl(xa: Transactor[IO]) extends TeamRepository{

  override def findAll(): IO[List[Team]] = 
    sql"""
      SELECT id, team_name, player_id 
      FROM teams
    """.query[(Option[Long], String, Option[Long])]
     .map{case (id,  team_name, player_id ) =>
     Team(id = id , name = team_name, lead = player_id.get)}
     .to[List].transact(xa)


  override def findById(id: Option[Long]): IO[Option[Team]] =
    sql"""
      SELECT id, team_name, player_id
      FROM teams 
      WHERE id = $id
    """.query[(Option[Long], String, Option[Long])]
       .map{case (id, team_name, player_id) =>
        Team(id = id , name = team_name, lead = player_id.get)}
       .option 
       .transact(xa)

   
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


class TeamGameRepository(xa: Transactor[IO]){

    def create(game_id: Option[Long], team_id: Option[Long]) = 
      sql"""
        INSERT INTO team_game (game_id, team_id)
        VALUES (${game_id}, ${team_id})
      """.update
         .run
         .transact(xa)


    def findAllTeams(): IO[List[Long]] = 
      sql"""
        SELECT team_id FROM team_game
      """.query[Long].to[List].transact(xa)
    

    def findAllGames(): IO[List[Long]] = 
      sql"""
        SELECT game_id FROM team_game
      """.query[Long].to[List].transact(xa)


    def delete_team(team_id: Option[Long]): IO[Int] = 
      sql"""
        DELETE FROM team_game WHERE team_id = $team_id
      """.update.run.transact(xa)


    def delete_game(game_id: Option[Long]): IO[Int] = 
      sql"""
        DELETE FROM team_game WHERE game_id = $game_id
      """.update.run.transact(xa)
}
