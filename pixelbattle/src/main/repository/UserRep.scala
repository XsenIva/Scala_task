import cats.effect.IO

import doobie._
import doobie.implicits._
import cats.implicits._

trait UserRepository{
  def findAll(): IO[List[User]]
  def findById(id: Int): IO[Option[User]]
  def insert(user: User): IO[Int]
  def delete(id: Int): IO[Int]
}


class UserRepositoryImpl(xa: Transactor[IO]) extends UserRepository {

  override def create(user: User): IO[Int] = 
    sql"""
      INSERT INTO users (name, email)
      VALUES (${user.name}, ${user.email})
    """.update.run.transact(xa)


  override def findById(id: Long): IO[Option[User]] = 
    sql"""
      SELECT id, name, email FROM users WHERE id = $id
    """.query[User].option.transact(xa)

  override def findAll: IO[List[User]] = 
    sql"""
      SELECT id, name, email FROM users
    """.query[User].to[List].transact(xa)

  override def update(user: User): IO[Int] = 
    sql"""
      UPDATE users 
      SET name = ${user.name}, email = ${user.email}
      WHERE id = ${user.id}
    """.update.run.transact(xa)


  override def delete(id: Long): IO[Int] = 
    sql"""
      DELETE FROM users WHERE id = $id
    """.update.run.transact(xa)
}