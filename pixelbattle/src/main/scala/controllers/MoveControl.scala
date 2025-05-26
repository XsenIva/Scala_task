package controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import service.GameService
import models.Move
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class MoveRequest(
  x: Int, 
  y: Int, 
  color: String)

case class ValidateRequest(
  canMove: Boolean,
  reason: String = "None"
)

class MoveRoutes(gameService: GameService) {

  val routes: Route = pathPrefix("move") {
    concat(

      pathEndOrSingleSlash {
        post {
          parameters("gameId".as[Long], "playerId".as[Long]) { (gameId, playerId) =>
            entity(as[MoveRequest]) { moveReq =>
              onSuccess(Future.successful(gameService.makeMove(gameId, playerId, moveReq.x, moveReq.y, moveReq.color))) {
                case Right(updatedMove) => complete(StatusCodes.OK -> updatedMove.asJson)
                case Left(error) => complete(StatusCodes.BadRequest -> Map("error" -> error).asJson)
              }
            }
          }
        }
      },

    
      path("validate") {
        post {
          parameters("gameId".as[Long], "playerId".as[Long]) { (gameId, playerId) =>
            entity(as[MoveRequest]) { moveReq =>
              onSuccess(Future.successful(gameService.canMakeMove(gameId, playerId, moveReq.x, moveReq.y))) {
                case Right(_) => complete(StatusCodes.OK -> ValidateRequest(true).asJson)
                case Left(error) => complete(StatusCodes.OK -> ValidateRequest(false, error).asJson)
                
              }
            }
          }
        }
      }
    )
  }
}
