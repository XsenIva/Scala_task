import doobie._
import doobie.implicits._
import cats.effect.IO
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import repository._
import models._
import cats.effect.unsafe.implicits.global

class UserRepositoryTest extends AnyFunSuite with BeforeAndAfterEach {
  private val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:userrepodb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "sa",
    ""
  )

  private var testUserCounter = 0

  private def setupDatabase(): Unit = {
    val dropTables = for {
      _ <- sql"DROP TABLE IF EXISTS sessions".update.run
      _ <- sql"DROP TABLE IF EXISTS moves".update.run
      _ <- sql"DROP TABLE IF EXISTS team_game".update.run
      _ <- sql"DROP TABLE IF EXISTS teams".update.run
      _ <- sql"DROP TABLE IF EXISTS games".update.run
      _ <- sql"DROP TABLE IF EXISTS players".update.run
    } yield ()

    val createTables = for {
      _ <- sql"""
        CREATE TABLE IF NOT EXISTS players (
          id BIGSERIAL PRIMARY KEY,
          username VARCHAR NOT NULL UNIQUE,
          logine VARCHAR NOT NULL,
          passwd VARCHAR NOT NULL
        )
      """.update.run
      _ <- sql"""
        CREATE TABLE IF NOT EXISTS games (
          id BIGSERIAL PRIMARY KEY,
          status_game VARCHAR NOT NULL,
          creation_date TIMESTAMP NOT NULL
        )
      """.update.run
      _ <- sql"""
        CREATE TABLE IF NOT EXISTS teams (
          id BIGSERIAL PRIMARY KEY,
          name VARCHAR NOT NULL,
          player_id BIGINT REFERENCES players(id)
        )
      """.update.run
      _ <- sql"""
        CREATE TABLE IF NOT EXISTS team_game (
          team_id BIGINT REFERENCES teams(id),
          game_id BIGINT REFERENCES games(id),
          PRIMARY KEY (team_id, game_id)
        )
      """.update.run
      _ <- sql"""
        CREATE TABLE IF NOT EXISTS sessions (
          id BIGSERIAL PRIMARY KEY,
          player_id BIGINT REFERENCES players(id),
          token VARCHAR NOT NULL,
          expiry TIMESTAMP NOT NULL
        )
      """.update.run
      _ <- sql"""
        CREATE TABLE IF NOT EXISTS moves (
          id BIGSERIAL PRIMARY KEY,
          game_id BIGINT REFERENCES games(id),
          player_id BIGINT REFERENCES players(id),
          x_coordinate INT NOT NULL,
          y_coordinate INT NOT NULL,
          color VARCHAR NOT NULL,
          move_time TIMESTAMP NOT NULL
        )
      """.update.run
    } yield ()
    
    (for {
      _ <- dropTables
      _ <- createTables
    } yield ()).transact(xa).unsafeRunSync()
  }

  override def beforeEach(): Unit = {
    setupDatabase()
    testUserCounter = 0
  }

  override def afterEach(): Unit = {
    testUserCounter = 0
  }

  val repo: UserRepository = new UserRepositoryImpl(xa)
  
  test("create user") {
    testUserCounter += 1
    val create: IO[Unit] = for {
      user <- repo.create(User(None, s"repo_test_user_${testUserCounter}", "test_email", "test_pass"))
      resp  <- repo.findById(user.id)
      _ <- IO {
        assert(resp.isDefined, "User should exist")
        assert(resp.get.name == s"repo_test_user_${testUserCounter}", "Username should match")
      }
    } yield ()
    create.unsafeRunSync()
  }

  test("update user") {
    testUserCounter += 1
    val update: IO[Unit] = for {
      user <- repo.create(User(None, s"repo_test_user_${testUserCounter}", "test_email", "test_pass"))
      user_2 = user.copy(name = s"repo_test_user_updated_${testUserCounter}")
      updateResult <- repo.update(user_2)
      user_updated <- repo.findById(user.id)
      _ <- IO {
        assert(updateResult == 1, "Update should affect one row")
        assert(user_updated.isDefined, "User should exist")
        assert(user_updated.get.name == s"repo_test_user_updated_${testUserCounter}", "Username should match")
      }
    } yield ()
    update.unsafeRunSync()
  }

  test("update non-existent user") {
    val update: IO[Unit] = for {
      updateResult <- repo.update(User(Some(999L), "non_existent", "test_email", "test_pass"))
      _ <- IO {
        assert(updateResult == 0, "Update should affect no rows")
      }
    } yield ()
    update.unsafeRunSync()
  }

  test("delete user") {
    testUserCounter += 1
    val delete: IO[Unit] = for {
      user <- repo.create(User(None, s"repo_test_user_${testUserCounter}", "test_email", "test_pass"))
      deleteResult <- repo.delete(user.id)
      userAfterDelete <- repo.findById(user.id)
      _ <- IO {
        assert(deleteResult == 1, "Delete should affect one row")
        assert(userAfterDelete.isEmpty, "User should not exist after deletion")
      }
    } yield ()
    delete.unsafeRunSync()
  }

  test("delete non-existent user") {
    val delete: IO[Unit] = for {
      deleteResult <- repo.delete(Some(999L))
      _ <- IO {
        assert(deleteResult == 0, "Delete should affect no rows")
      }
    } yield ()
    delete.unsafeRunSync()
  }
}