package repository

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import doobie._
import doobie.implicits._
import models.Game
import java.time.LocalDateTime
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global

class GameRepositorySpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  val transactor: Resource[IO, Transactor[IO]] = for {
    xa <- Resource.pure[IO, Transactor[IO]](
      Transactor.fromDriverManager[IO](
        driver = "org.h2.Driver",
        url = "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        user = "sa",
        pass = ""
      )
    )
    _ <- Resource.eval(setupDatabase(xa))
  } yield xa

  // Create test table before each test
  def setupDatabase(xa: Transactor[IO]): IO[Unit] = {
    sql"""
      DROP TABLE IF EXISTS games;
      CREATE TABLE games (
        id BIGSERIAL PRIMARY KEY,
        status_game VARCHAR NOT NULL,
        creation_date TIMESTAMP NOT NULL
      )
    """.update.run.transact(xa).void
  }

  "GameRepository" should "create and retrieve a game" in {
    val testGame = Game(None, "ACTIVE", LocalDateTime.now())
    
    (for {
      xa <- transactor.allocated
      repository = new GameRepositoryImpl(xa._1)
      created <- repository.create(testGame)
      retrieved <- repository.findById(created.id)
      _ <- xa._2
    } yield {
      retrieved.isDefined shouldBe true
      retrieved.get.status shouldBe testGame.status
      retrieved.get.id.isDefined shouldBe true
    }).unsafeToFuture()
  }

  it should "find all games" in {
    val testGame1 = Game(None, "ACTIVE", LocalDateTime.now())
    val testGame2 = Game(None, "FINISHED", LocalDateTime.now())
    
    (for {
      xa <- transactor.allocated
      repository = new GameRepositoryImpl(xa._1)
      _ <- repository.create(testGame1)
      _ <- repository.create(testGame2)
      allGames <- repository.findAll()
      _ <- xa._2
    } yield {
      allGames.length shouldBe 2
      allGames.exists(_.status == "ACTIVE") shouldBe true
      allGames.exists(_.status == "FINISHED") shouldBe true
    }).unsafeToFuture()
  }

  it should "delete a game" in {
    val testGame = Game(None, "ACTIVE", LocalDateTime.now())
    
    (for {
      xa <- transactor.allocated
      repository = new GameRepositoryImpl(xa._1)
      created <- repository.create(testGame)
      deleteCount <- repository.delete(created.id)
      retrieved <- repository.findById(created.id)
      _ <- xa._2
    } yield {
      deleteCount shouldBe 1
      retrieved shouldBe None
    }).unsafeToFuture()
  }
} 