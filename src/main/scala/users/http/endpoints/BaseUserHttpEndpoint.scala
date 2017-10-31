package users.http.endpoints

import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import users.http.Middleware

import scala.concurrent.TimeoutException

class BaseUserHttpEndpoint(middleware: Middleware[IO]) extends Http4sClientDsl[IO] {

  val service: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "users" => middleware.all().attempt.unsafeRunSync() match {
      case Right(users)               => Ok(users.asJson)
      case Left(e: TimeoutException)  => InternalServerError(e.getMessage)
      case Left(_)                    => InternalServerError("Unexpected error")
    }
  }

}
