package users.http

import cats.effect.IO
import cats.syntax.semigroupk._
import fs2.Stream
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.util.StreamApp
import users.ApplicationContext
import users.http.endpoints.{AdminUserHttpEndpoint, BaseUserHttpEndpoint}

object Server extends StreamApp[IO] {

  private val underlyingUserService = ApplicationContext.application.services.userManagement
  private val middleware            = Middleware[IO](underlyingUserService)

  private val baseUserHttpEndpoint  = new BaseUserHttpEndpoint(middleware)
  private val adminUserHttpEndpoint = new AdminUserHttpEndpoint(middleware)

  private val notAuthService: HttpService[IO] =
    baseUserHttpEndpoint.service <+> adminUserHttpEndpoint.loginService

  private val httpServices = Router[IO](
    "/" -> notAuthService,
    s"/${endpoints.ApiVersion}/admin" -> adminUserHttpEndpoint.service
  )

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
      .mountService(httpServices)
      .serve

}
