package users.http.endpoints

import java.time._

import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.semigroupk._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import org.reactormonk.{CryptoBits, PrivateKey}
import users.domain.User
import users.http.Middleware
import users.http.endpoints.model.UserLogin

class AdminUserHttpEndpoint[F[_] : Effect](middleware: Middleware[F]) extends HttpEndpoint[F] {

  private val adminUser = UserLogin("gvolpe", "") // Hardcoded admin user

  private def retrieveUser: Kleisli[F, String, UserLogin] =
    Kleisli(username => Sync[F].delay(UserLogin(username, "")))

  private val onFailure: AuthedService[String, F] = Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))

  private val authUser: Kleisli[F, Request[F], Either[String, UserLogin]] = Kleisli({ request =>
    val message = for {
      header  <- headers.Cookie.from(request.headers).toRight("Cookie parsing error")
      cookie  <- header.values.toList.find(_.name == "authcookie").toRight("Couldn't find the authcookie")
      token   <- crypto.validateSignedToken(cookie.content).toRight("Cookie invalid")
      message <- Right(token)
    } yield message
    message.traverse(retrieveUser.run)
  })

  private val authMiddleware: AuthMiddleware[F, UserLogin] = AuthMiddleware(authUser, onFailure)

  private val key     = PrivateKey(scala.io.Codec.toUTF8(scala.util.Random.alphanumeric.take(20).mkString("")))
  private val crypto  = CryptoBits(key)
  private val clock   = Clock.systemUTC

  // TODO: Retrieve user from real admin db...
  private def verifyLogin(userLogin: UserLogin): F[String Either UserLogin] =
    userLogin match {
      case user @ UserLogin(adminUser.username, _)  => Sync[F].delay(Right(user))
      case _                                        => Sync[F].delay(Left("User must be admin"))
    }

  private def login: Kleisli[F, Request[F], Response[F]] = Kleisli({ request =>
    request.decode[UserLogin] { userLogin =>
      verifyLogin(userLogin).>>= {
        case Left(error) =>
          Forbidden(error)
        case Right(user) => {
          val message = crypto.signToken(user.username, clock.millis.toString)
          Ok("Logged in!").map(_.addCookie(Cookie("authcookie", message)))
        }
      }
    }
  })

  private val authedService: AuthedService[UserLogin, F] = AuthedService {
    case GET -> Root / "users" as _ =>
      middleware.all().handle
    case DELETE -> Root / "users" / id as _ =>
      middleware.delete(User.Id(id)).handle
    case POST -> Root / "users" / id / "block" as _ =>
      middleware.block(User.Id(id)).handle
    case POST -> Root /  "users" / id / "unblock" as _ =>
      middleware.unblock(User.Id(id)).handle
  }

  val loginService : HttpService[F] = HttpService[F] {
    case req @ POST -> Root / ApiVersion / "signin" =>
      login.run(req)
  }

  val service: HttpService[F] = authMiddleware(authedService)

}
