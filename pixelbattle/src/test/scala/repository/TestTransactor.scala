package repository

import cats.effect.{IO, Resource}
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._

trait TestTransactor {
  def setupDatabase(xa: Transactor[IO]): IO[Unit]

  lazy val transactor: Resource[IO, Transactor[IO]] = Resource.make(
    IO(Transactor.fromDriverManager[IO](
      "org.h2.Driver",
      "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "sa",
      ""
    ))
  )(xa => IO.unit) 
} 