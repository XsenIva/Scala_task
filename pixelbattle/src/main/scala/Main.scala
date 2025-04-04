import cats.effect._
import doobie._
import doobie.implicits._
import resources._

import doobie.hikari.HikariTransactor

object Main extends IOApp {

  private def runMigrations(): IO[Unit] = IO {
    val flyway =  resources.FlywayMigration.migrate()
  }

  private def createTransactor(): Resource[IO, HikariTransactor[IO]] = {
    resources.Database.transactor
  }

  private def testDoobieQuery(transactor: HikariTransactor[IO]): IO[Unit] = {
    sql"SELECT datname FROM pg_database;"
      .query[String]
      .option
      .transact(transactor)
      .flatMap {
        case Some(user) => IO(println(s"Найден пользователь: $user"))
        case None => IO(println("Пользователи не найдены"))
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