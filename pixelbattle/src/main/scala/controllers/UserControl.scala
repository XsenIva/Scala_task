package controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import service.{GameService, UserService}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class RegisterRequest(
  username: String, 
  email: String,
  password: String)

case class LoginRequest(
  email: String, 
  password: String)


class PlayerRoutes(gameService: GameService, userService: UserService) {

  val routes: Route = pathPrefix("players") {
    concat(
      path("register") {
        post {
          entity(as[RegisterRequest]) { request =>
            println(s"Received registration request: username=${request.username}, email=${request.email}")
            println(s"Full request data: $request")
            onSuccess(Future.successful(userService.registerUser(request.username, request.email, request.password))) {
              case Right(user) => 
                println(s"Registration successful: $user")
                complete(StatusCodes.Created -> user.asJson)
              case Left(error) => 
                println(s"Registration failed with error: $error")
                complete(StatusCodes.BadRequest -> ValidateRequest(false, error).asJson)
            }
          }
        }
      },
      path(LongNumber) { userId =>
        get {
          onSuccess(Future.successful(userService.getUserById(userId))) {
            case Some(user) => complete(StatusCodes.OK -> user.asJson)
            case None => complete(StatusCodes.NotFound -> ValidateRequest(false, "User not found").asJson)
          }
        }
      },
      path("join") {
        post {
          parameters("playerId".as[Long]) { playerId =>
            val game = gameService.addPlayer(playerId)
            complete(StatusCodes.OK -> game.asJson)
          }
        }
      },
      path("score" / LongNumber) { playerId =>
        get {
          val score = gameService.getUserScore(playerId)
          complete(StatusCodes.OK -> ValidateRequest(true, score.toString).asJson)
        }
      }
    )
  }
}
