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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Game", description = "Game management endpoints")
class GameRoutes(gameService: GameService) {

  @Operation(
    summary = "Create a new game",
    description = "Creates a new pixel battle game instance",
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Game created successfully",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[GameField])
          )
        )
      )
    )
  )
  def createGame: Route = post {
    val game = gameService.createGame()
    complete(StatusCodes.OK -> game.asJson)
  }

  @Operation(
    summary = "Get game by ID",
    description = "Retrieves a game instance by its ID",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Game found"),
      new ApiResponse(responseCode = "404", description = "Game not found")
    )
  )
  def getGameById(id: Long): Route = get {
    val gameField = gameService.getGameById(id)
    complete(
      gameField match {
        case Some(field) => StatusCodes.OK -> field.asJson
        case None => StatusCodes.NotFound -> s"Game with id $id not found"
      }
    )
  }

  @Operation(
    summary = "Get current game",
    description = "Retrieves the current active game instance",
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Current game retrieved successfully",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[GameField])
          )
        )
      )
    )
  )
  def getCurrentGame: Route = get {
    val gameField = Future.successful(gameService.getCurrentGame())
    onSuccess(gameField) { field =>
      complete(StatusCodes.OK -> field.asJson)
    }
  }

  @Operation(
    summary = "Place a pixel",
    description = "Places a pixel on the game field with specified coordinates and color",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Pixel placed successfully"),
      new ApiResponse(responseCode = "400", description = "Invalid parameters or move"),
      new ApiResponse(responseCode = "404", description = "No active game found")
    )
  )
  def placePixel: Route = {
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

  val routes: Route = pathPrefix("game") {
    concat(
      pathEndOrSingleSlash {
        createGame
      },
      path(LongNumber) { id =>
        getGameById(id)
      },
      get {
        getCurrentGame
      },
      path("pixel") {
        placePixel
      }
    )
  }

  private def currentGameId: Option[Long] = gameService.getCurrentGameId
}
