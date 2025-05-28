import cats.effect._
import doobie._
import doobie.implicits._
import db._
import doobie.hikari.HikariTransactor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import controller.{GameRoutes, PlayerRoutes}
import service.{GameServiceImpl, UserServiceImpl, MoveServiceImpl}
import repository.{GameRepositoryImpl, UserRepositoryImpl, TeamRepositoryImpl, MoveRepositoryImpl}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import akka.http.scaladsl.model.HttpMethods._

object Main extends IOApp {

  private def runMigrations(): IO[Unit] = IO {
    val flyway = db.FlywayMigration.migrate()
  }

  private def createTransactor(): Resource[IO, HikariTransactor[IO]] = {
    db.Database.transactor
  }

  def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Future[Http.ServerBinding] = {
    import system.executionContext

    val corsSettings = CorsSettings.defaultSettings
      .withAllowedMethods(List(GET, POST, PUT, DELETE, OPTIONS))
      .withAllowCredentials(true)
      .withMaxAge(Some(86400L)) // 24 часа в секундах

    val corsRoutes = CorsDirectives.cors(corsSettings) {
      routes
    }

    Http().newServerAt("localhost", 8080)
      .bind(corsRoutes)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val system = ActorSystem(Behaviors.empty, "PixelBattleSystem")
    implicit val executionContext: ExecutionContext = system.executionContext

    createTransactor().use { transactor =>
      for {
        _ <- runMigrations()
        
        // Repositories
        userRepository = new UserRepositoryImpl(transactor)
        gameRepository = new GameRepositoryImpl(transactor)
        teamRepository = new TeamRepositoryImpl(transactor)
        moveRepository = new MoveRepositoryImpl(transactor)

        // Services
        moveService = new MoveServiceImpl(moveRepository, gameRepository, userRepository)
        gameService = new GameServiceImpl(gameRepository, teamRepository, moveRepository)
        userService = new UserServiceImpl(userRepository)

        // Routes
        gameRoutes = new GameRoutes(gameService)
        playerRoutes = new PlayerRoutes(gameService, userService)

        // Combine routes
        routes = gameRoutes.routes ~ playerRoutes.routes

        // Start server
        _ <- IO.fromFuture(IO(startHttpServer(routes))).map { binding =>
          println(s"Server online at http://localhost:8080/")
          binding
        }

        // Keep the server running
        result <- IO.never[ExitCode]

      } yield result
    }
  }
}