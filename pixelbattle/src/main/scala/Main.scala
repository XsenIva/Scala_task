import cats.effect._
import doobie._
import doobie.implicits._
import db._

import doobie.hikari.HikariTransactor

object Main extends IOApp {

  private def runMigrations(): IO[Unit] = IO {
    val flyway =  db.FlywayMigration.migrate()
  }

  private def createTransactor(): Resource[IO, HikariTransactor[IO]] = {
    db.Database.transactor
  }

  private def testDoobieQuery(transactor: HikariTransactor[IO]): IO[Unit] = {
    sql"SELECT * FROM players;"
      .query[String]
      .option 
      .transact(transactor)
      .flatMap {
        case Some(user) => IO(println(s"Найден игрок!"))
        case None => IO(println("Не найден игрок"))
      } 
  }

  // Главный метод
  override def run(args: List[String]): IO[ExitCode] = {
    createTransactor().use { transactor =>
      for {
        _ <- runMigrations()
        _ <- testDoobieQuery(transactor)
      } yield ExitCode.Success
    }
  }
}