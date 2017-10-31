package users.http

import cats.effect.Effect
import users.ApplicationContext
import users.domain.User.Id
import users.domain._
import users.services.usermanagement.Error

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object Middleware {
  def apply[F[_] : Effect]: Middleware[F] = new Middleware[F]()
}

/**
  * The purpose of this class is to wrap the existent [[users.services.usermanagement.Algebra]] that models
  * all the methods to the type F[Error Either A] and just use F[A] since where F is any [[cats.effect.Effect]].
  *
  * Preferably, I'd change the type of the algebra's methods to F[A] : Effect.
  * */
class Middleware[F[_] : Effect] {

  implicit class IOFutureInterop[A](futureOfEither: Future[Either[Error, A]]) {
    def toF(implicit timeOut: FiniteDuration): F[A] = Effect[F].async { cb =>
      cb(Await.result(futureOfEither, timeOut))
    }
  }

  private val underlyingUserService = ApplicationContext.application.services.userManagement

  implicit val serviceTimeOut: FiniteDuration = 2.seconds

  def generateId(): F[Id] = Effect[F].async { cb =>
    Try(Await.result(underlyingUserService.generateId(), serviceTimeOut)) match {
      case Success(id) => cb(Right(id))
      case Failure(e)  => cb(Left(e))
    }
  }

  def get(id: User.Id): F[User] = underlyingUserService.get(id).toF

  def signUp(userName: UserName, emailAddress: EmailAddress, password: Option[Password]): F[User] =
    underlyingUserService.signUp(userName, emailAddress, password).toF

  def updateEmail(id: User.Id, emailAddress: EmailAddress): F[User] =
    underlyingUserService.updateEmail(id, emailAddress).toF

  def updatePassword(id: User.Id, password: Password): F[User] =
    underlyingUserService.updatePassword(id, password).toF

  def resetPassword(id: User.Id): F[User] = underlyingUserService.resetPassword(id).toF

  def block(id: User.Id): F[User] = underlyingUserService.block(id).toF

  def unblock(id: User.Id): F[User] = underlyingUserService.unblock(id).toF

  def delete(id: User.Id): F[Done] = underlyingUserService.delete(id).toF

  def all(): F[List[User]] = underlyingUserService.all().toF

}
