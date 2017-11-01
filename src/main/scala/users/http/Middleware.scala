package users.http

import cats.effect.Effect
import users.domain._
import users.services.UserManagement
import users.services.usermanagement.Error

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Middleware {
  def apply[F[_] : Effect](underlyingUserService: UserManagement[Future[?]]): Middleware[F] =
    new Middleware[F](underlyingUserService)

  implicit class IOFutureInterop[A](futureOfEither: Future[Either[Error, A]]) {
    def toF[F[_] : Effect](implicit timeOut: FiniteDuration): F[A] =
      Effect[F].async { cb =>
        cb(Await.result(futureOfEither, timeOut))
      }
  }

}

/**
  * The purpose of this class is to wrap the existent [[users.services.usermanagement.Algebra]], where all the methods
  * are modelled with the type F[Error Either A], and just use F[A] where F is any [[cats.effect.Effect]].
  *
  * Preferably, I'd instantiate [[users.services.UserManagement]] as F[_] : Effect and write an interpreter for F.
  **/
class Middleware[F[_] : Effect](underlyingUserService: UserManagement[Future[?]]) {

  import Middleware._

  implicit val serviceTimeOut: FiniteDuration = 2.seconds

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
