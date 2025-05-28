package repository

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import doobie._
import doobie.implicits._
import models.Game
import java.time.LocalDateTime
import cats.effect.Resource
import doobie.hikari.HikariTransactor

class GameRepositorySpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with TestitTransactor {
  // Clean up test data before each test
  override def setupDatabase(xa: HikariTransactor[IO]): IO[Unit] = {
    sql"""
      DELETE FROM team_game WHERE game_id IN (SELECT id FROM games WHERE status_game LIKE 'TEST_%');
      DELETE FROM moves WHERE game_id IN (SELECT id FROM games WHERE status_game LIKE 'TEST_%');
      DELETE FROM games WHERE status_game LIKE 'TEST_%';
    """.update.run.transact(xa).void
  }

  // Создаем репозиторий для тестов
  def withRepository[A](test: GameRepositoryImpl => IO[A]): IO[A] = {
    transactor.use { xa =>
      setupDatabase(xa) *>
      test(new GameRepositoryImpl(xa))
    }
  }

  "GameRepository" - {
    "should create and retrieve a game" in {
      val testGame = Game(None, "TEST_ACTIVE", LocalDateTime.now())
      
      withRepository { repository =>
        for {
          created <- repository.create(testGame)
          retrieved <- repository.findById(created.id)
        } yield {
          retrieved.isDefined shouldBe true
          retrieved.get.status shouldBe testGame.status
          retrieved.get.id.isDefined shouldBe true
        }
      }
    }

    "should find all test games" in {
      val testGame1 = Game(None, "TEST_ACTIVE", LocalDateTime.now())
      val testGame2 = Game(None, "TEST_FINISHED", LocalDateTime.now())
      
      withRepository { repository =>
        for {
          _ <- repository.create(testGame1)
          _ <- repository.create(testGame2)
          allGames <- repository.findAll()
          testGames = allGames.filter(_.status.startsWith("TEST_"))
        } yield {
          testGames.length shouldBe 2
          testGames.exists(_.status == "TEST_ACTIVE") shouldBe true
          testGames.exists(_.status == "TEST_FINISHED") shouldBe true
        }
      }
    }

    "should delete a game" in {
      val testGame = Game(None, "TEST_ACTIVE", LocalDateTime.now())
      
      withRepository { repository =>
        for {
          created <- repository.create(testGame)
          deleteCount <- repository.delete(created.id)
          retrieved <- repository.findById(created.id)
        } yield {
          deleteCount shouldBe 1
          retrieved shouldBe None
        }
      }
    }
  }
} 