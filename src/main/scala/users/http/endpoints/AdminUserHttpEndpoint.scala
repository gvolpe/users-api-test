package users.http.endpoints

import java.time._

import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats.syntax.either._
import cats.syntax.semigroupk._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import org.reactormonk.{CryptoBits, PrivateKey}
import users.domain.User
import users.http.Middleware
import users.http.endpoints.model.UserLogin

class AdminUserHttpEndpoint(middleware: Middleware[IO]) extends Http4sClientDsl[IO] {

  private def retrieveUser: Kleisli[IO, String, UserLogin] =
    Kleisli(username => IO(UserLogin(username, "")))

  private val onFailure: AuthedService[IO, String] = Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))

  private val authUser: Kleisli[IO, Request[IO], Either[String, UserLogin]] = Kleisli({ request =>
    val message = for {
      header  <- headers.Cookie.from(request.headers).toRight("Cookie parsing error")
      cookie  <- header.values.toList.find(_.name == "authcookie").toRight("Couldn't find the authcookie")
      token   <- crypto.validateSignedToken(cookie.content).toRight("Cookie invalid")
      message <- Right(token)
    } yield message
    message.traverse(retrieveUser.run)
  })

  private val authMiddleware: AuthMiddleware[IO, UserLogin] = AuthMiddleware(authUser, onFailure)

  private val key     = PrivateKey(scala.io.Codec.toUTF8(scala.util.Random.alphanumeric.take(20).mkString("")))
  private val crypto  = CryptoBits(key)
  private val clock   = Clock.systemUTC

  // TODO: Retrieve user from real admin db...
  private def verifyLogin(userLogin: UserLogin): IO[String Either UserLogin] =
    userLogin match {
      case user @ UserLogin("gvolpe", _)  => IO(Right(user))
      case _                              => IO(Left("User must be admin"))
    }

  private def login: Kleisli[IO, Request[IO], Response[IO]] = Kleisli({ request =>
    request.decode[UserLogin] { userLogin =>
      verifyLogin(userLogin).flatMap {
        case Left(error) =>
          Forbidden(error)
        case Right(user) => {
          val message = crypto.signToken(user.username, clock.millis.toString)
          Ok("Logged in!").map(_.addCookie(Cookie("authcookie", message)))
        }
      }
    }
  })

  import IOHttpResponseHandler._

  val authedService: AuthedService[IO, UserLogin] = AuthedService {
    case DELETE -> Root / ApiVersion / "admin" /  "users" / id as _ =>
      middleware.delete(User.Id(id)).handle
    case POST -> Root / ApiVersion / "admin" /  "users" / id / "block" as _ =>
      middleware.block(User.Id(id)).handle
    case POST -> Root / ApiVersion / "admin" /  "users" / id / "unblock" as _ =>
      middleware.unblock(User.Id(id)).handle
  }

  val loginService : HttpService[IO] = HttpService[IO] {
    case req @ POST -> Root / ApiVersion / "signin" =>
      login.run(req)
  }

  val service: HttpService[IO] = loginService <+> authMiddleware(authedService)

}
