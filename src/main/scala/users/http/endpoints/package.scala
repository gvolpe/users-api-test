package users.http

import java.time.OffsetDateTime

import io.circe.{Encoder, Json}
import users.domain.{EmailAddress, Password, User, UserName}

package object endpoints {

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
