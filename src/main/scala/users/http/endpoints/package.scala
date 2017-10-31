package users.http

import java.time.OffsetDateTime

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.{Encoder, Json}
import org.http4s.EntityDecoder
import org.http4s.circe._
import users.domain.{EmailAddress, Password, User, UserName}

package object endpoints {

  val ApiVersion = "v1"

  object model {
    case class UserSignUp(username: String, email: String, password: Option[String])
    case class UserPatch(email: Option[String], password: Option[String])

    implicit def userSignUpDecoder: EntityDecoder[IO, UserSignUp] = jsonOf[IO, UserSignUp]
    implicit def userPatchDecoder: EntityDecoder[IO, UserPatch] = jsonOf[IO, UserPatch]
  }

  implicit val userIdEncoder: Encoder[User.Id] = Encoder.instance {
    case User.Id(id) => Json.fromString(id)
  }

  implicit val userNameEncoder: Encoder[UserName] = Encoder.instance {
    case UserName(name) => Json.fromString(name)
  }

  implicit val userEmailEncoder: Encoder[EmailAddress] = Encoder.instance {
    case EmailAddress(email) => Json.fromString(email)
  }

  implicit val userPasswordEncoder: Encoder[Password] = Encoder.instance {
    case Password(pwd) => Json.fromString(pwd)
  }

  implicit val offsetDateTimeEncoder: Encoder[OffsetDateTime] = Encoder.instance {
    d => Json.fromString(d.toString)
  }

}
