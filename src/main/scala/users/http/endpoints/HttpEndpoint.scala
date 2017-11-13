package users.http.endpoints

import cats.effect._
import io.circe.Encoder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import users.services.usermanagement.Error.{Active, Blocked, Deleted, Exists, NotFound => UserNotFound, System => UserSystem}

import scala.concurrent.TimeoutException

object HttpEndpoint {
  def apply[F[_] : Effect]: HttpEndpoint[F] = new HttpEndpoint[F]()
}

class HttpEndpoint[F[_] : Effect] extends Http4sDsl[F] {

  val usersErrorHandler: PartialFunction[Throwable, F[Response[F]]] = {
    case e: TimeoutException  => InternalServerError(e.getMessage)
    case Exists               => Conflict()
    case UserNotFound         => NotFound()
    case Deleted              => NotFound("User is deleted")
    case Active               => BadRequest("User is active")
    case Blocked              => BadRequest("User is blocked")
    case UserSystem(t)        => InternalServerError(t.getMessage)
  }

  implicit class EffectHandlerOps[A](effect: F[A]) {
    import cats.syntax.applicativeError._
    import cats.syntax.flatMap._

    def handle(implicit encoder: Encoder[A]): F[Response[F]] =
      effect.>>=(x => Ok(x.asJson)).recoverWith(usersErrorHandler)

    def handleCreation(implicit encoder: Encoder[A]): F[Response[F]] =
      effect.>>=(x => Created(x.asJson)).recoverWith(usersErrorHandler)
  }

}
