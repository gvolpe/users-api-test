package users.http.endpoints

import cats.effect.IO
import org.http4s.EntityBody
import users.config.{ApplicationConfig, ExecutorsConfig, ServicesConfig}
import users.http.Middleware
import users.main.Application

trait HttpEndpointSpec {

  private val config = ApplicationConfig(
    executors = ExecutorsConfig(
      services = ExecutorsConfig.ServicesConfig(
        parallellism = 4
      )
    ),
    services = ServicesConfig(
      users = ServicesConfig.UsersConfig(
        failureProbability = 0.0,
        timeoutProbability = 0.0
      )
    )
  )

  private val application = Application.fromApplicationConfig.run(config)
  private val underlyingUserService = application.services.userManagement
  val middleware: Middleware[IO] = Middleware[IO](underlyingUserService)

  implicit class ByteVector2String(body: EntityBody[IO]) {
    def asString: String = {
      val array = body.runLog.unsafeRunSync().toArray
      new String(array.map(_.toChar))
    }
  }

  implicit class BodyStringOps(body: String) {
    def userId: String = body.slice(7, 7 + 36)
  }

}