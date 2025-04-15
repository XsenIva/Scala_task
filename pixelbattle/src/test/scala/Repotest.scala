import doobie._
import doobie.implicits._
import cats.effect.IO
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import repository._
import models._
import cats.effect.unsafe.implicits.global

class UserRepositoryTest extends AnyFunSuite with BeforeAndAfterEach {
  val xa: Transactor[IO] = 
    Transactor.fromDriverManager[IO](
        "org.postgresql.Driver", 
        "jdbc:postgresql://localhost:5435/pixelbattle", 
        "admin", 
        "password"
    )


  override def afterEach(): Unit = {
    sql"DELETE FROM players".update.run.transact(xa).unsafeRunSync()
  } 
  
  
  val repo: UserRepository = new UserRepositoryImpl(xa)
  
  test("create user") {
     val create: IO[Unit] = for {
       user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
       resp  <- repo.findById(user.id)
       _ <- IO {
        assert(resp.isDefined, "User should exist")
        assert(resp.get.name == "test_user", "Username should match")
      }
    } yield ()
    create.unsafeRunSync()
  }


  test ("update user"){
    val update:  IO[Unit] = for {
      user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
      user_2 = user.copy(name = "test_ABOBBA")
      _ <- repo.update(user_2)
      user_aboba <- repo.findById(user_2.id)
      _ <- IO{
        assert(user_aboba.isDefined, "User should exist")
        assert(user_aboba.get.name == "test_ABOBBA", "Username should match")
      }
    } yield ()
     update.unsafeRunSync()
  }


  test ("delete user"){
    val delete: IO[Unit] = for  {
      user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
      resp <- repo.findById(user.id)
      res  <- repo.delete(resp.get.id)
      _ <- IO{
        assert(res == 1)
      }
    } yield ()
    delete.unsafeRunSync()
  }
}



class RepositoryTest extends AnyFunSuite with BeforeAndAfterEach {
  val xa: Transactor[IO] = 
    Transactor.fromDriverManager[IO](
        "org.postgresql.Driver", 
        "jdbc:postgresql://localhost:5435/pixelbattle", 
        "admin", 
        "password"
    )


  override def afterEach(): Unit = {
    sql"DELETE FROM players".update.run.transact(xa).unsafeRunSync()
  } 
  
  
  val repo: UserRepository = new UserRepositoryImpl(xa)
  
  test("create user") {
     val create: IO[Unit] = for {
       user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
       resp  <- repo.findById(user.id)
       _ <- IO {
        assert(resp.isDefined, "User should exist")
        assert(resp.get.name == "test_user", "Username should match")
      }
    } yield ()
    create.unsafeRunSync()
  }


  test ("update user"){
    val update:  IO[Unit] = for {
      user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
      user_2 = user.copy(name = "test_ABOBBA")
      _ <- repo.update(user_2)
      user_aboba <- repo.findById(user_2.id)
      _ <- IO{
        assert(user_aboba.isDefined, "User should exist")
        assert(user_aboba.get.name == "test_ABOBBA", "Username should match")
      }
    } yield ()
     update.unsafeRunSync()
  }


  test ("delete user"){
    val delete: IO[Unit] = for  {
      user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
      resp <- repo.findById(user.id)
      res  <- repo.delete(resp.get.id)
      _ <- IO{
        assert(res == 1)
      }
    } yield ()
    delete.unsafeRunSync()
  }
}


