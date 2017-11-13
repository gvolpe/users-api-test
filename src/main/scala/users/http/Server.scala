package users.http

import cats.effect.{Effect, IO}
import cats.syntax.semigroupk._
import fs2.Stream
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.util.{ExitCode, StreamApp}
import users.ApplicationContext
import users.http.endpoints.{AdminUserHttpEndpoint, BaseUserHttpEndpoint}

object Server extends HttpServer[IO]

class HttpServer[F[_] : Effect] extends StreamApp[F] {

  private val underlyingUserService = ApplicationContext.application.services.userManagement
  private val middleware            = Middleware[F](underlyingUserService)

  private val baseUserHttpEndpoint  = new BaseUserHttpEndpoint[F](middleware)
  private val adminUserHttpEndpoint = new AdminUserHttpEndpoint[F](middleware)

  private val notAuthService: HttpService[F] =
    baseUserHttpEndpoint.service <+> adminUserHttpEndpoint.loginService

  private val httpServices = Router[F](
    "/" -> notAuthService,
    s"/${endpoints.ApiVersion}/admin" -> adminUserHttpEndpoint.service
  )

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    BlazeBuilder[F]
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
      .mountService(httpServices)
      .serve

}
