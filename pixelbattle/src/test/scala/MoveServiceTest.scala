import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfterEach
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import java.time.{LocalDateTime, Duration}
import service._
import repository._
import models._


class MoveServiceTest extends AnyFunSuite with BeforeAndAfterEach {
  
  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:moveservicedb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "sa",
    ""
  )

  val moveRepository = new MoveRepositoryImpl(xa)
  val gameRepository = new GameRepositoryImpl(xa)
  val userRepository = new UserRepositoryImpl(xa)
  val moveService = new MoveServiceImpl(
    moveRepository, 
    gameRepository, 
    userRepository,
    Duration.ofSeconds(1) 
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

  def createTestUser(): User = {
    testUserCounter += 1
    val user = User(None, s"move_test_user_${testUserCounter}", "test@email.com", "password123")
    userRepository.create(user).unsafeRunSync()
  }

  def createTestGame(): Game = {
    val game = Game(None, "active", LocalDateTime.now())
    gameRepository.create(game).unsafeRunSync()
  }

  // Tests
  test("makeMove - successful move") {
    val user = createTestUser()
    val game = createTestGame()
    
    val result = moveService.makeMove(
      game.id.get,
      user.id.get,
      x = 5,
      y = 5,
      color = Color.Red.hex
    )
    
    assert(result.isRight, "Move should be successful")
    result.foreach { move =>
      assert(move.x == 5)
      assert(move.y == 5)
      assert(move.color == Color.Red.hex)
      assert(move.gameid == game.id.get)
      assert(move.playerid == user.id.get)
    }
  }

  test("makeMove - invalid coordinates") {
    val user = createTestUser()
    val game = createTestGame()
    
    val result = moveService.makeMove(
      game.id.get,
      user.id.get,
      x = -1,
      y = -1,
      color = Color.Red.hex
    )
    
    assert(result.isLeft)
    assert(result.left.getOrElse(null) == MoveError.InvalidCoordinates)
  }

  test("makeMove - invalid color") {
    val user = createTestUser()
    val game = createTestGame()
    
    val result = moveService.makeMove(
      game.id.get,
      user.id.get,
      x = 5,
      y = 5,
      color = "#INVALID"
    )
    
    assert(result.isLeft)
    assert(result.left.getOrElse(null) == MoveError.InvalidColor)
  }

  test("makeMove - time limit restriction") {
    val user = createTestUser()
    val game = createTestGame()
    
    // First move should succeed
    val firstMove = moveService.makeMove(
      game.id.get,
      user.id.get,
      x = 5,
      y = 5,
      color = Color.Red.hex
    )
    assert(firstMove.isRight)

    // Second immediate move should fail
    val secondMove = moveService.makeMove(
      game.id.get,
      user.id.get,
      x = 6,
      y = 6,
      color = Color.Blue.hex
    )
    assert(secondMove.isLeft)
    assert(secondMove.left.getOrElse(null) == MoveError.TimeLimit)
  }

  test("makeMove - game not found") {
    val user = createTestUser()
    
    val result = moveService.makeMove(
      gameid = 999L,
      user.id.get,
      x = 5,
      y = 5,
      color = Color.Red.hex
    )
    
    assert(result.isLeft)
    assert(result.left.getOrElse(null) == MoveError.GameNotFound)
  }

  test("makeMove - player not found") {
    val game = createTestGame()
    
    val result = moveService.makeMove(
      game.id.get,
      playerid = 999L,
      x = 5,
      y = 5,
      color = Color.Red.hex
    )
    
    assert(result.isLeft)
    assert(result.left.getOrElse(null) == MoveError.PlayerNotFound)
  }

  test("getMove - retrieve existing move") {
    val user = createTestUser()
    val game = createTestGame()
    
    val createResult = moveService.makeMove(
      game.id.get,
      user.id.get,
      x = 5,
      y = 5,
      color = Color.Red.hex
    )
    assert(createResult.isRight)

    val retrievedMove = moveService.getMove(5, 5)
    assert(retrievedMove.isDefined)
    retrievedMove.foreach { move =>
      assert(move.x == 5)
      assert(move.y == 5)
      assert(move.color == Color.Red.hex)
      assert(move.gameid == game.id.get)
      assert(move.playerid == user.id.get)
    }
  }

  test("getMove - non-existing coordinates") {
    val move = moveService.getMove(99, 99)
    assert(move.isEmpty)
  }

  test("getField - retrieve all moves") {
    val user = createTestUser()
    val game = createTestGame()
    
    val firstMove = moveService.makeMove(game.id.get, user.id.get, 1, 1, Color.Red.hex)
    assert(firstMove.isRight)
    
    Thread.sleep(1100) 
    
    val secondMove = moveService.makeMove(game.id.get, user.id.get, 2, 2, Color.Blue.hex)
    assert(secondMove.isRight)
    
    val field = moveService.getField()
    assert(field.length == 2)
    assert(field.exists(m => m.x == 1 && m.y == 1 && m.color == Color.Red.hex))
    assert(field.exists(m => m.x == 2 && m.y == 2 && m.color == Color.Blue.hex))
  }

  test("getLastMoveTime - get last move time for player") {
    val user = createTestUser()
    val game = createTestGame()
    
    val beforeMove = LocalDateTime.now()
    Thread.sleep(1000) 
    
    val moveResult = moveService.makeMove(game.id.get, user.id.get, 5, 5, Color.Red.hex)
    assert(moveResult.isRight)
    
    val lastMoveTime = moveService.getLastMoveTime(user.id.get)
    assert(lastMoveTime.isDefined)
    lastMoveTime.foreach { time =>
      assert(time.isAfter(beforeMove))
    }
  }

  test("getLastMoveTime - no moves for player") {
    val user = createTestUser()
    val lastMoveTime = moveService.getLastMoveTime(user.id.get)
    assert(lastMoveTime.isEmpty)
  }
} 