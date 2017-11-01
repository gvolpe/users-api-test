package users.http.endpoints

import cats.effect.IO
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.{FlatSpecLike, Matchers}

// TODO: Add the remaining case scenarios
class BaseUserHttpEndpointSpec extends HttpEndpointSpec with FlatSpecLike with Matchers {

  behavior of "BaseUserHttpEndpoint"

  val httpService = new BaseUserHttpEndpoint(middleware).service

  it should "retrieve all users" in {
    val request = Request[IO](uri = Uri(path = s"/$ApiVersion/users"))

    val task = httpService(request).value.unsafeRunSync()
    task.foreach { response =>
      response.status         should be (Status.Ok)
      response.body.asString  should be ("[]")
    }
  }

  it should "create a new user" in {
    val body =
      """
        |{
        |  "username": "admin",
        |  "email": "admin@gmail.com"
        |}
      """.stripMargin

    val request = Request[IO](method = Method.POST, uri = Uri(path = s"/$ApiVersion/signup"))
      .withBody[String](body).unsafeRunSync()

    val task = httpService(request).value.unsafeRunSync()
    task.foreach { response =>
      response.status         should be (Status.Created)
    }
  }

  it should "retrieve user by id" in {
    val body =
      """
        |{
        |  "username": "gvolpe",
        |  "email": "gvolpe@gmail.com",
        |  "password": "123456"
        |}
      """.stripMargin

    val createReq = Request[IO](method = Method.POST, uri = Uri(path = s"/$ApiVersion/signup"))
      .withBody[String](body).unsafeRunSync()

    val createTask = httpService(createReq).value.unsafeRunSync()
    createTask.foreach { createResponse =>
      val userId = createResponse.body.asString.userId
      val request = Request[IO](uri = Uri(path = s"/$ApiVersion/users/$userId"))

      val task = httpService(request).value.unsafeRunSync()
      task.foreach { response =>
        response.status               should be (Status.Ok)
        response.body.asString.userId should be (userId)
      }
    }
  }

  it should "NOT retrieve user by id" in {
    val request = Request[IO](uri = Uri(path = s"/$ApiVersion/users/123"))

    val task = httpService(request).value.unsafeRunSync()
    task.foreach { response =>
      response.status         should be (Status.NotFound)
    }
  }

  it should "NOT create a duplicated user" in {
    val body =
      """
        |{
        |  "username": "modersky",
        |  "email": "modersky@gmail.com"
        |}
      """.stripMargin

    val createReq = Request[IO](method = Method.POST, uri = Uri(path = s"/$ApiVersion/signup"))
      .withBody[String](body).unsafeRunSync()

    httpService(createReq).value.unsafeRunSync()

    val task = httpService(createReq).value.unsafeRunSync()
    task.foreach { response =>
      response.status               should be (Status.Conflict)
    }
  }

}
