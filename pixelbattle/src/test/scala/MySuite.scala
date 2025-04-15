import doobie._
import doobie.implicits._
import cats.effect.IO
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class UserRepositoryTest extends AnyFunSuite with BeforeAndAfterEach {
  val xa: Transactor[IO] = 
    Transactor.fromDriverManager[IO](
        "org.postgresql.Driver", 
        "jdbc:postgresql://localhost:5435/pixelbattle", 
        "admin", 
        "password"
    )
  
  val repo: UserRepository = new UserRepositoryImpl(xa)
  

  test("create user") {
    val user = User("test_user", "test_pass")
    repo.create(user).unsafeRunSync()

    val newUser = repo.findById(user.id).unsafeRunSync()
    assert(newUser.exists(_.name == "test_user"))
  }

  
  test ("findById user"){
    val user = User("test_ABOBA", "test_pass")
    repo.create(user).unsafeRunSync()

    val newUser = repo.findById(user.id).unsafeRunSync()
    assert(newUser.exists(_.name == "test_ABOBA"))
  }


  test ("update user"){
    val user = User(0, "test_ABOBA", "test_pass")
    repo.update(user).unsafeRunSync()

    val newUser = repo.findById(user.id).unsafeRunSync()
    assert(newUser.exists(_.name == "test_ABOBA"))
  }

  test ("delete user"){
    val user = User(1, "test_ABOBA", "test_pass")
    repo.create(user).unsafeRunSync()

    val newUser = repo.findById(user.id).unsafeRunSync()
    assert(newUser.exists(_.name == "test_ABOBA"))
  }

  

}

