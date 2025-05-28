package repository

import cats.effect.IO
import doobie._
import doobie.implicits._
import cats.implicits._
import models.User

trait UserRepository {
  def findAll(): IO[List[User]]
  def findById(id: Option[Long]): IO[Option[User]]
  def create(user: User): IO[User]
  def update(user: User): IO[Int]
  def delete(id: Option[Long]): IO[Int]
}

class UserRepositoryImpl(xa: Transactor[IO]) extends UserRepository {

  override def findAll(): IO[List[User]] = 
    sql"""
      SELECT id, username, logine, passwd 
      FROM players
    """.query[(Option[Long], String, String, String)]
       .map{case (id, username, logine, password) =>
         User(id = id, name = username, email = logine, passwordHash = password)}
       .to[List].transact(xa)

  override def findById(id: Option[Long]): IO[Option[User]] = {
    id match {
      case Some(userId) =>
        println(s"Looking up user with ID: $userId")
        val query = sql"""
          SELECT id, username, logine, passwd 
          FROM players 
          WHERE id = $userId
        """
        println(s"Executing SQL query: ${query.query[(Option[Long], String, String, String)].sql}")
        
        query.query[(Option[Long], String, String, String)]
           .map{case (id, username, logine, password) =>
             User(id = id, name = username, email = logine, passwordHash = password)}
           .option 
           .transact(xa)
           .map { result =>
             println(s"Database lookup result for user $userId: $result")
             result
           }
      case None => 
        println("findById called with None")
        IO.pure(None)
    }
  }

  override def create(user: User): IO[User] = {
    println(s"Starting database transaction for user creation: ${user}")
    val insertQuery = sql"""
      INSERT INTO players (username, logine, passwd)
      VALUES (${user.name}, ${user.email}, ${user.passwordHash})
    """
    println(s"Attempting to insert user into database")
    
    insertQuery.update
       .withUniqueGeneratedKeys[Long]("id")
       .map { id => 
         println(s"Generated ID for new user: $id")
         user.copy(id = Some(id))
       }
       .transact(xa)
       .handleErrorWith { error =>
         println(s"Database error during user creation: ${error.getMessage}")
         error.printStackTrace()
         IO.raiseError(error)
       }
  }

  override def update(user: User): IO[Int] = {
    user.id match {
      case Some(userId) =>
        sql"""
          UPDATE players
          SET username = ${user.name}, 
              logine = ${user.email}, 
              passwd = ${user.passwordHash}
          WHERE id = $userId
        """.update
           .run
           .transact(xa)
      case None => IO.pure(0)
    }
  }

  override def delete(id: Option[Long]): IO[Int] = {
    id match {
      case Some(userId) =>
        (for {
          _ <- sql"DELETE FROM sessions WHERE player_id = $userId".update.run
          _ <- sql"DELETE FROM moves WHERE player_id = $userId".update.run
          _ <- sql"DELETE FROM teams WHERE player_id = $userId".update.run
          result <- sql"DELETE FROM players WHERE id = $userId".update.run
        } yield result).transact(xa)
      case None => IO.pure(0)
    }
  }
}

