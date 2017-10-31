package users.http.endpoints

import cats.effect._
import io.circe.Encoder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

import scala.concurrent.TimeoutException

object IOHttpResponseHandler {

  implicit class IOHandlerOps[A](io: IO[A]) {
    def handle(implicit encoder: Encoder[A]): IO[Response[IO]] = io.attempt.unsafeRunSync() match {
      case Right(users)               => Ok(users.asJson)
      case Left(e: TimeoutException)  => InternalServerError(e.getMessage)
      case Left(_)                    => InternalServerError("Unexpected error")
    }
  }

}
