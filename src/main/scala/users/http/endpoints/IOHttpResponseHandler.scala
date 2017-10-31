package users.http.endpoints

import cats.effect._
import io.circe.Encoder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.impl.EntityResponseGenerator
import org.http4s.dsl.io._
import users.services.usermanagement.Error.{Active, Blocked, Deleted, Exists, NotFound => UserNotFound, System => UserSystem}
import users.services.usermanagement.{Error => UserError}

import scala.concurrent.TimeoutException

object IOHttpResponseHandler {

  implicit class IOHandlerOps[A](io: IO[A]) {

    private def baseHandler(successfulResponse: EntityResponseGenerator[IO])(implicit encoder: Encoder[A]): IO[Response[IO]] =
      io.attempt.unsafeRunSync() match {
        case Right(users)               => successfulResponse.apply(users.asJson)
        case Left(e: TimeoutException)  => InternalServerError(e.getMessage)
        case Left(e: UserError)         => e match {
          case Exists         => Conflict()
          case UserNotFound   => NotFound()
          case Deleted        => NotFound("User is deleted")
          case Active         => BadRequest("User is active")
          case Blocked        => BadRequest("User is blocked")
          case UserSystem(t)  => InternalServerError(t.getMessage)
        }
        case Left(_)                    => InternalServerError("Unexpected error")
      }

    def handle(implicit encoder: Encoder[A]): IO[Response[IO]] = baseHandler(Ok)

    def handleCreation(implicit encoder: Encoder[A]): IO[Response[IO]] = baseHandler(Created)

  }

}
