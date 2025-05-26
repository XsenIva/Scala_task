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

class GameRepositorySpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with TestTransactor {
  // Create test table before each test
  override def setupDatabase(xa: Transactor[IO]): IO[Unit] = {
    sql"""
      DROP TABLE IF EXISTS games;
      CREATE TABLE games (
        id BIGSERIAL PRIMARY KEY,
        status_game VARCHAR NOT NULL,
        creation_date TIMESTAMP NOT NULL
      )
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
      val testGame = Game(None, "ACTIVE", LocalDateTime.now())
      
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

    "should find all games" in {
      val testGame1 = Game(None, "ACTIVE", LocalDateTime.now())
      val testGame2 = Game(None, "FINISHED", LocalDateTime.now())
      
      withRepository { repository =>
        for {
          _ <- repository.create(testGame1)
          _ <- repository.create(testGame2)
          allGames <- repository.findAll()
        } yield {
          allGames.length shouldBe 2
          allGames.exists(_.status == "ACTIVE") shouldBe true
          allGames.exists(_.status == "FINISHED") shouldBe true
        }
      }
    }

    "should delete a game" in {
      val testGame = Game(None, "ACTIVE", LocalDateTime.now())
      
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