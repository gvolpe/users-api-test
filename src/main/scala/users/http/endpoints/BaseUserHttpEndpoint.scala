package users.http.endpoints

import cats.effect._
import cats.syntax.flatMap._
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s._
import users.domain.{EmailAddress, Password, User, UserName}
import users.http.Middleware
import users.http.endpoints.model.{UserPatch, UserSignUp}

class BaseUserHttpEndpoint[F[_] : Effect](middleware: Middleware[F]) extends HttpEndpoint[F] {

  implicit val baseUserEncoder: Encoder[User] =
    Encoder.forProduct3("id", "username", "email")(u =>
      (u.id, u.userName, u.emailAddress)
    )

  val service: HttpService[F] = HttpService[F] {
    case req @ POST -> Root / ApiVersion / "signup" =>
      req.decode[UserSignUp] { user =>
        middleware.signUp(
          UserName(user.username),
          EmailAddress(user.email),
          user.password.map(Password.apply)
        ).handleCreation
      }

    case GET -> Root / ApiVersion / "users" => middleware.all().handle

    case GET -> Root / ApiVersion / "users" / id => middleware.get(User.Id(id)).handle

    case req @ PATCH -> Root / ApiVersion / "users" / id =>
      req.decode[UserPatch] { user =>
        (user.email, user.password) match {
          case (Some(email), _)     => middleware.updateEmail(User.Id(id), EmailAddress(email)).handle
          case (_, Some(password))  => middleware.updatePassword(User.Id(id), Password(password)).handle
          case (_, _)               => BadRequest("Either email or password can be updated.")
        }
      }

    case POST -> Root / ApiVersion / "users" / id / "forgotpassword" =>
      middleware.resetPassword(User.Id(id)).handle
  }

}
