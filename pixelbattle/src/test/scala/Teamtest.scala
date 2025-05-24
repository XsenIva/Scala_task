// import doobie._
// import doobie.implicits._
// import cats.effect.IO
// import org.scalatest.BeforeAndAfterEach
// import org.scalatest.funsuite.AnyFunSuite
// import repository._
// import models._
// import cats.effect.unsafe.implicits.global

// val xa: Transactor[IO] = 
//     Transactor.fromDriverManager[IO](
//         "org.postgresql.Driver", 
//         "jdbc:postgresql://localhost:5435/pixelbattle", 
//         "admin", 
//         "password"
//     )


// class TeamRepositoryTest extends AnyFunSuite with BeforeAndAfterEach {

//   override def afterEach(): Unit = {
//     sql"DELETE FROM players".update.run.transact(xa).unsafeRunSync()
//   } 
  
  
//   val repo: TeamRepository = new TeamRepositoryImpl(xa)
  
//   test("create user") {
//      val create: IO[Unit] = for {
//        user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
//        resp  <- repo.findById(user.id)
//        _ <- IO {
//         assert(resp.isDefined, "User should exist")
//         assert(resp.get.name == "test_user", "Username should match")
//       }
//     } yield ()
//     create.unsafeRunSync()
//   }


//   test ("update user"){
//     val update:  IO[Unit] = for {
//       user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
//       user_2 = user.copy(name = "test_ABOBBA")
//       _ <- repo.update(user_2)
//       user_aboba <- repo.findById(user_2.id)
//       _ <- IO{
//         assert(user_aboba.isDefined, "User should exist")
//         assert(user_aboba.get.name == "test_ABOBBA", "Username should match")
//       }
//     } yield ()
//      update.unsafeRunSync()
//   }


//   test ("delete user"){
//     val delete: IO[Unit] = for  {
//       user <- repo.create(User(None,"test_user", "test_email", "test_pass"))
//       resp <- repo.findById(user.id)
//       res  <- repo.delete(resp.get.id)
//       _ <- IO{
//         assert(res == 1)
//       }
//     } yield ()
//     delete.unsafeRunSync()
//   }

// }

// class TeamRepositoryTest extends AnyFunSuite with BeforeAndAfterEach {
 
//   override def afterEach(): Unit = {
//     sql"DELETE FROM teams".update.run.transact(xa).unsafeRunSync()
//   } 

//   val repo: TeamRepository = new TeamRepositoryImpl(xa)
  
//   test("create team") {
//      val create: IO[Unit] = for {
//        team <- repo.create(Team(None, "test_team", 1))
//        resp  <- repo.findById(team.id)
//        _ <- IO {
//         assert(resp.isDefined, "Team should exist")
//         assert(resp.get.name == "test_team", "Team name should match")
//       }
//     } yield ()
//     create.unsafeRunSync()
//   }

//   test ("delete team"){
//     val delete: IO[Unit] = for  {
//       team <- repo.create(Team(None,"test_team", 1))
//       resp <- repo.findById(team.id)
//       res  <- repo.delete(resp.get.id)
//       _ <- IO{
//         assert(res == 1)
//       }
//     } yield ()
//     delete.unsafeRunSync()
//   }
  
// }