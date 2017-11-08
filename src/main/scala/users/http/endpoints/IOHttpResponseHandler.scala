package users.http.endpoints

import cats.effect._
import io.circe.Encoder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import users.services.usermanagement.Error.{Active, Blocked, Deleted, Exists, NotFound => UserNotFound, System => UserSystem}

import scala.concurrent.TimeoutException

object IOHttpResponseHandler {

  val usersErrorHandler: PartialFunction[Throwable, IO[Response[IO]]] = {
    case e: TimeoutException  => InternalServerError(e.getMessage)
    case Exists               => Conflict()
    case UserNotFound         => NotFound()
    case Deleted              => NotFound("User is deleted")
    case Active               => BadRequest("User is active")
    case Blocked              => BadRequest("User is blocked")
    case UserSystem(t)        => InternalServerError(t.getMessage)
  }

  implicit class IOHandlerOps[A](io: IO[A]) {
    import cats.syntax.applicativeError._

    def handle(implicit encoder: Encoder[A]): IO[Response[IO]] =
      io.flatMap(x => Ok(x.asJson)).recoverWith(usersErrorHandler)

    def handleCreation(implicit encoder: Encoder[A]): IO[Response[IO]] =
      io.flatMap(x => Created(x.asJson)).recoverWith(usersErrorHandler)
  }

}
