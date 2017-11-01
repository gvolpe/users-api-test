package users.http.endpoints

import cats.effect.IO
import org.http4s.{Cookie, Method, Request, Status, Uri}
import org.scalatest.{FlatSpecLike, Matchers}

// TODO: Add the remaining case scenarios
class AdminUserHttpEndpointSpec extends HttpEndpointSpec with FlatSpecLike with Matchers {

  behavior of "AdminUserHttpEndpoint"

  val httpService = new AdminUserHttpEndpoint(middleware).service

  it should "should NOT authorize access to a base user" in {
    val request = Request[IO](method = Method.DELETE, uri = Uri(path = s"/$ApiVersion/admin/users/123"))

    val task = httpService(request).value.unsafeRunSync()
    task.foreach { response =>
      response.status         should be (Status.Forbidden)
    }
  }

  it should "signin the admin user" in {
    val body =
      """
        |{
        |  "username": "gvolpe",
        |  "password": ""
        |}
      """.stripMargin

    val request = Request[IO](method = Method.POST, uri = Uri(path = s"/$ApiVersion/signin"))
      .withBody[String](body).unsafeRunSync()

    val task = httpService(request).value.unsafeRunSync()
    task.foreach { response =>
      val cookieHeader = response.headers.filter(_.value.contains("authcookie"))
      val cookieValue = cookieHeader.map(_.value).toList.headOption.getOrElse("").split('=')(1)

      response.status         should be (Status.Ok)

      val adminReq = Request[IO](method = Method.DELETE, uri = Uri(path = s"/$ApiVersion/admin/users/123"))
        .withHeaders(cookieHeader).addCookie(Cookie("authcookie", cookieValue))

      httpService(adminReq).value.unsafeRunSync().foreach { adminResponse =>
        adminResponse.status should be (Status.NotFound) // User 123 does not exist
      }
    }
  }

}
