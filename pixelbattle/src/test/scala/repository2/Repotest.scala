import doobie._
import doobie.implicits._
import cats.effect.IO
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import repository._
import models._
import cats.effect.unsafe.implicits.global
import doobie.hikari.HikariTransactor
import cats.implicits._

class UserRepositoryTestit extends AnyFunSuite with BeforeAndAfterAll with TestitTransactor {
  private var testUserCounter = 0
  private var xa: HikariTransactor[IO] = _
  private var repo: UserRepository = _
  private var release: IO[Unit] = _

override def beforeAll(): Unit = {
  val allocated = transactor.allocated.unsafeRunSync()
  xa = allocated._1
  release = allocated._2
  repo = new UserRepositoryImpl(xa)
}

override def afterAll(): Unit = {
  if (release != null) {
    release.unsafeRunSync()
  }
}

  override def setupDatabase(xa: HikariTransactor[IO]): IO[Unit] = {
    val cleanupTestData = for {
      _ <- sql"""DELETE FROM sessions WHERE player_id IN (SELECT id FROM players WHERE username LIKE 'repo_test_user_%')""".update.run
      _ <- sql"""DELETE FROM moves WHERE player_id IN (SELECT id FROM players WHERE username LIKE 'repo_test_user_%')""".update.run
      _ <- sql"""DELETE FROM team_game WHERE team_id IN (SELECT t.id FROM teams t JOIN players p ON t.player_id = p.id WHERE p.username LIKE 'repo_test_user_%')""".update.run
      _ <- sql"""DELETE FROM teams WHERE player_id IN (SELECT id FROM players WHERE username LIKE 'repo_test_user_%')""".update.run
      _ <- sql"""DELETE FROM players WHERE username LIKE 'repo_test_user_%'""".update.run
    } yield ()

    cleanupTestData.transact(xa)
  }

  test("create user") {
    testUserCounter += 1
    val username = s"repo_test_user_${testUserCounter}"
    val result = for {
      _ <- setupDatabase(xa)
      user <- repo.create(User(None, username, "test_email", "test_pass"))
      resp <- repo.findById(user.id)
    } yield (user, resp)

    val (user, resp) = result.unsafeRunSync()
    assert(resp.isDefined, "User should exist")
    assert(resp.get.name == username, "Username should match")
    assert(user.id.isDefined, "User should have an ID")
  }

  test("update user") {
    testUserCounter += 1
    val username = s"repo_test_user_${testUserCounter}"
    val updatedUsername = s"repo_test_user_updated_${testUserCounter}"
    
    val result = for {
      _ <- setupDatabase(xa)
      user <- repo.create(User(None, username, "test_email", "test_pass"))
      user2 = user.copy(name = updatedUsername)
      updateResult <- repo.update(user2)
      userUpdated <- repo.findById(user.id)
    } yield (updateResult, userUpdated)

    val (updateResult, userUpdated) = result.unsafeRunSync()
    assert(updateResult == 1, "Update should affect one row")
    assert(userUpdated.isDefined, "User should exist")
    assert(userUpdated.get.name == updatedUsername, "Username should match")
  }

  test("update non-existent user") {
    val result = for {
      _ <- setupDatabase(xa)
      updateResult <- repo.update(User(Some(999L), "non_existent", "test_email", "test_pass"))
    } yield updateResult

    assert(result.unsafeRunSync() == 0, "Update should affect no rows")
  }

  test("delete user") {
    testUserCounter += 1
    val username = s"repo_test_user_${testUserCounter}"
    
    val result = for {
      _ <- setupDatabase(xa)
      user <- repo.create(User(None, username, "test_email", "test_pass"))
      deleteResult <- repo.delete(user.id)
      userAfterDelete <- repo.findById(user.id)
    } yield (deleteResult, userAfterDelete)

    val (deleteResult, userAfterDelete) = result.unsafeRunSync()
    assert(deleteResult == 1, "Delete should affect one row")
    assert(userAfterDelete.isEmpty, "User should not exist after deletion")
  }

  test("delete non-existent user") {
    val result = for {
      _ <- setupDatabase(xa)
      deleteResult <- repo.delete(Some(999L))
    } yield deleteResult

    assert(result.unsafeRunSync() == 0, "Delete should affect no rows")
  }
}