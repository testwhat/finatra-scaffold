package com.github.finatrascaffold

import com.github.xiaodongw.swagger.finatra.SwaggerSupport
import com.twitter.finatra.http.Controller

class BaseController extends Controller with Logging with SwaggerSupport {
  implicit protected val swagger = SwaggerInstance
}
