package controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import service.GameService
import service.GameField
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.server.ExceptionHandler

class GameRoutes(gameService: GameService) {

  val routes: Route = pathPrefix("game") {
    concat(
      pathEndOrSingleSlash {
        post {
          val game = gameService.createGame()
          complete(StatusCodes.OK -> game.asJson)
        }
      },
      path(LongNumber) { id =>
        get {
          val gameField = gameService.getGameById(id)
          complete(
            gameField match {
              case Some(field) => StatusCodes.OK -> field.asJson
              case None => StatusCodes.NotFound -> s"Game with id $id not found"
            }
          )
        }
      },
      get {
        val gameField = Future.successful(gameService.getCurrentGame())
        onSuccess(gameField) { field =>
          complete(StatusCodes.OK -> field.asJson)
        }
      },
      path("pixel") {
        post {
          handleExceptions(ExceptionHandler {
            case e: NumberFormatException =>
              println(s"Number format exception: ${e.getMessage}")
              complete(StatusCodes.BadRequest -> "Invalid parameters")
          }) {
            parameters("x".as[Int], "y".as[Int], "color", "player_id".as[Long]) { (x, y, color, playerId) =>
              println(s"Received pixel placement request: x=$x, y=$y, color=$color, playerId=$playerId")
              currentGameId match {
                case Some(gameId) =>
                  println(s"Current game ID: $gameId")
                  onSuccess(Future.successful(gameService.makeMove(gameId, playerId, x, y, color))) {
                    case Right(updatedMove) => 
                      println(s"Successfully placed pixel: $updatedMove")
                      complete(StatusCodes.OK -> updatedMove.asJson)
                    case Left(error) => 
                      println(s"Failed to place pixel: $error")
                      complete(StatusCodes.BadRequest -> error)
                  }
                case None =>
                  println("No active game found")
                  complete(StatusCodes.NotFound -> "No active game found")
              }
            } ~ {
              println("Missing or invalid parameters in request")
              complete(StatusCodes.BadRequest -> "Missing or invalid parameters")
            }
          }
        }
      }
    )
  }

  private def currentGameId: Option[Long] = gameService.getCurrentGameId
}
