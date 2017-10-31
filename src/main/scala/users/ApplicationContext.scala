package users

import cats.data._
import cats.effect.IO
import users.config._
import users.domain.{EmailAddress, User, UserName}
import users.main._

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}

object ApplicationContext {

  val config = ApplicationConfig(
    executors = ExecutorsConfig(
      services = ExecutorsConfig.ServicesConfig(
        parallellism = 4
      )
    ),
    services = ServicesConfig(
      users = ServicesConfig.UsersConfig(
        failureProbability = 0.1,
        timeoutProbability = 0.1
      )
    )
  )

  val application = Application.fromApplicationConfig.run(config)

  // TODO: Import the specific EC
//  import scala.concurrent.ExecutionContext.Implicits.global
//
//  val result = for {
//    _     <- EitherT(userService.signUp(UserName("gvolpe"), EmailAddress("gvolpe@github.com"), None))
//    users <- EitherT(userService.all())
//  } yield users
//
//  val io = IO.async[List[User]] { cb =>
//    cb(Await.result(result.value, 2.seconds))
//  }
//
//  io.attempt.unsafeRunSync() match {
//    case Right(users)               => println(users)
//    case Left(e: TimeoutException)  => println(e)
//    case Left(_)                    => println("Unexpected error")
//  }

}
