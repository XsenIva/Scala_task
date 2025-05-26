package controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import controller.GameRoutes
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import service.{GameService, GameField}
import models.Move
import org.mockito.MockitoSugar
import java.time.LocalDateTime
import json.Codecs._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

class GameControlSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with MockitoSugar {

  val mockGameService: GameService = mock[GameService]
  val gameRoutes = new GameRoutes(mockGameService)

  val testGameField = GameField.defaultField()
  val testMove = Move(Some(1L), 1L, 1L, 5, 5, "#FF0000", LocalDateTime.now())

  "GameRoutes" should {
    "create a new game (POST /game)" in {
      when(mockGameService.createGame())
        .thenReturn(testGameField)

      Post("/game") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[GameField] shouldBe testGameField
      }
    }

    "get game by id (GET /game/{id})" in {
      when(mockGameService.getGameById(1L))
        .thenReturn(Some(testGameField))

      Get("/game/1") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[GameField] shouldBe testGameField
      }

      when(mockGameService.getGameById(999L))
        .thenReturn(None)

      Get("/game/999") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.NotFound
        responseAs[String] shouldBe "Game with id 999 not found"
      }
    }

    "get current game (GET /game)" in {
      when(mockGameService.getCurrentGame())
        .thenReturn(testGameField)

      Get("/game") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[GameField] shouldBe testGameField
      }
    }

    "handle pixel placement (POST /game/pixel)" in {
      when(mockGameService.getCurrentGameId)
        .thenReturn(Some(1L))
      
      when(mockGameService.makeMove(1L, 1L, 5, 5, "#FF0000"))
        .thenReturn(Right(testMove))

      Post("/game/pixel?x=5&y=5&color=#FF0000&player_id=1") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Move] shouldBe testMove
      }

      when(mockGameService.makeMove(1L, 1L, -1, -1, "#FF0000"))
        .thenReturn(Left("Invalid coordinates"))

      Post("/game/pixel?x=-1&y=-1&color=#FF0000&player_id=1") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[String] shouldBe "Invalid coordinates"
      }

      when(mockGameService.getCurrentGameId)
        .thenReturn(None)

      Post("/game/pixel?x=5&y=5&color=#FF0000&player_id=1") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.NotFound
        responseAs[String] shouldBe "No active game found"
      }
    }

    "handle invalid pixel parameters" in {
      when(mockGameService.getCurrentGameId)
        .thenReturn(Some(1L))

      Post("/game/pixel") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[String] shouldBe "Missing or invalid parameters"
      }

      Post("/game/pixel?x=abc&y=5&color=#FF0000&player_id=1") ~> gameRoutes.routes ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[String] shouldBe "Invalid parameters"
      }
    }
  }
} 