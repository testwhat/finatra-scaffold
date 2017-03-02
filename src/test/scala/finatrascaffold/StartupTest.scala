package finatrascaffold

import javax.inject.Singleton

import com.github.finatrascaffold.{BaseController, BaseServer}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.inject.Test

@Singleton
class Controller extends BaseController {
  get("/test") { _: Request =>
    response.ok("ok")
  }
}

class Server extends BaseServer {
  override def configureHttpRouter(router: HttpRouter) = {
    router.add[Controller]
  }
}

class StartupTest extends Test {
  val server = new EmbeddedHttpServer(twitterServer = new Server)

  "StartupTest" in {
    server.assertHealthy()
    server.httpGet("/test").contentString should equal("ok")
  }
}
