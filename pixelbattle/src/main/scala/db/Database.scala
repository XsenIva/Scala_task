package db
import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Database {
  def transactor: Resource[IO, HikariTransactor[IO]] = {
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](size = 10)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver", 
        "jdbc:postgresql://localhost:5435/pixelbattle", 
        "admin", 
        "password",
        ec 
      )
    } yield xa
  }
}