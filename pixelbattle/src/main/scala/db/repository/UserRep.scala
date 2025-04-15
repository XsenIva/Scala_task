package db.repository


import cats.effect.IO

import doobie._
import doobie.implicits._
import cats.implicits._
import models.User


trait UserRepository{
  def findAll(): IO[List[User]]
  def findById(id: Long): IO[Option[User]]
  def create(user: User): IO[Int]
  def update(user: User): IO[Int]
  def delete(id: Long): IO[Int]
}


class UserRepositoryImpl(xa: Transactor[IO]) extends UserRepository {

  override def findAll(): IO[List[User]] = 
    sql"""
      SELECT id, username, logine, password 
      FROM players
    """.query[(Option[Long], String, String, String)]
       .map{case (id, username, logine, password) =>
        User(id = id, name = username, email = logine, passwordHash = password)}
       .to[List].transact(xa)

  override def findById(id: Option[Long]): IO[User] =
    sql"""
      SELECT id, username, logine, password 
      FROM players 
      WHERE id = $id
    """.query[(Option[Long], String, String, String)]
       .map{case (id, username, logine, password) =>
        User(id = id, name = username, email = logine, passwordHash = password)}
       .option.transact(xa)


  override def create(user: User): IO[Option[Long]] = 
    sql"""
      INSERT INTO players (username, logine, password)
      VALUES (${user.name}, ${user.email}, ${user.passwordHash})
    """.update
       .withUniqueGeneratedKeys[Long]("id")
       .map(id => user.copy(id = id))
       .transact(xa)


  override def update(user: User): IO[Int] = 
    sql"""
      UPDATE players
      SET username = ${user.name}, 
      logine = ${user.email}, 
      password = ${user.passwordHash},
      WHERE id = ${user.id}
    """.update.run.transact(xa)


  override def delete(id: Option[Long]): IO[Int] = 
    sql"""
      DELETE FROM players WHERE id = $id
    """.update.run.transact(xa)
}

