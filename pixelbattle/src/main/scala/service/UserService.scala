package service

import repository.UserRepository
import models.{User, Team}
import cats.effect.unsafe.implicits.global

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
          Right(userRepository.create(user).unsafeRunSync())
        } catch {
          case e: Exception => Left(s"Failed to register user: ${e.getMessage}")
        }
    }
  }

  override def getUserById(id: Long): Option[User] = {
    userRepository.findById(Some(id)).unsafeRunSync()
  }
}