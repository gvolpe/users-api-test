package users.http

import cats.effect.IO
import fs2.Stream
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import users.http.endpoints.BaseUserHttpEndpoint

object Server extends StreamApp[IO] {

  val middleware: Middleware[IO] = Middleware[IO]

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
      .mountService(new BaseUserHttpEndpoint(middleware).service)
      .serve

}
