package users.http.endpoints

import cats.effect._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import users.domain.User
import users.http.Middleware

class BaseUserHttpEndpoint(middleware: Middleware[IO]) extends Http4sClientDsl[IO] {

  import IOHttpResponseHandler._

  val service: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "users" => middleware.all().handle

    case GET -> Root / "users" / id => middleware.get(User.Id(id)).handle
  }

}
