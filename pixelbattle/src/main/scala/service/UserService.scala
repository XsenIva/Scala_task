package service

import repository.UserRepository
import models.{User, Team}
import cats.effect.unsafe.implicits.global
import java.time.LocalDateTime
import java.security.MessageDigest
import java.util.Base64

case class LeaderboardEntry(userId: Long, username: String, score: Int)

trait UserService {
  def registerUser(name: String, email: String, passwordHash: String): Either[String, User]
  def getUserById(id: Long): Option[User]
}

class UserServiceImpl(userRepository: UserRepository) extends UserService {

  private def validateRegistration(name: String, email: String, passwordHash: String): Option[String] = {
    if (name.trim.isEmpty) {
      Some("Username cannot be empty")
    } else if (name.length < 3 || name.length > 50) {
      Some("Username must be between 3 and 50 characters")
    } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      Some("Invalid email format")
    } else if (passwordHash.length < 6) {
      Some("Password hash is too short")
    } else {
      None
    }
  }

  override def registerUser(name: String, email: String, passwordHash: String): Either[String, User] = {
    validateRegistration(name, email, passwordHash) match {
      case Some(error) => Left(error)
      case None =>
        try {
          val user = User(None, name, email, passwordHash)
          println(s"Attempting to create user: $user")
          val createdUser = userRepository.create(user).unsafeRunSync()
          println(s"Successfully created user: $createdUser")
          
          // Verify the user was actually saved in the database
          val verificationResult = userRepository.findById(createdUser.id).unsafeRunSync()
          println(s"Database verification result: ${verificationResult}")
          
          verificationResult match {
            case Some(dbUser) => 
              println(s"User found in database: $dbUser")
              Right(createdUser)
            case None => 
              println("WARNING: User was not found in database after creation!")
              Left("User creation failed: Unable to verify user in database")
          }
        } catch {
          case e: Exception => 
            println(s"Failed to register user: ${e.getMessage}")
            e.printStackTrace()
            Left(s"Failed to register user: ${e.getMessage}")
        }
    }
  }

  override def getUserById(id: Long): Option[User] = {
    userRepository.findById(Some(id)).unsafeRunSync()
  }
}