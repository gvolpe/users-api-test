package users.persistence.repositories.users

import cats.syntax.eq._
import users.domain._
import users.persistence.repositories._

import scala.collection.mutable
import scala.concurrent.Future

private[users] object InMemoryRepository {
  private final var UserMap = mutable.HashMap.empty[User.Id, User]
}

private[users] class InMemoryRepository extends UserRepository {
  import InMemoryRepository._

  def insert(user: User): Future[Done] =
    Future.successful {
      UserMap += (user.id → user)
      Done
    }

  def get(id: User.Id): Future[Option[User]] =
    Future.successful(UserMap.get(id))

  def getByUserName(userName: UserName): Future[Option[User]] =
    Future.successful {
      UserMap.collectFirst {
        case (_, user) if user.userName === userName ⇒ user
      }
    }

  def all(): Future[List[User]] =
    Future.successful(UserMap.values.toList)
}
