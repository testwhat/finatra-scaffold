package com.github.finatrascaffold

import com.github.xiaodongw.swagger.finatra.{SwaggerController, WebjarsController}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters._
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.twitter.finatra.json.utils.CamelCasePropertyNamingStrategy

abstract class BaseServer extends HttpServer {

  override def jacksonModule = new FinatraJacksonModule {
    override val propertyNamingStrategy = CamelCasePropertyNamingStrategy
  }

  val enableSwaggerUi = false
  val propertyClassPaths = Seq.empty

  override def configureHttp(router: HttpRouter) {
    router
      .filter[ExceptionMappingFilter[Request]]
      .filter[AccessLoggingFilter[Request]]
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
    configureHttpRouter(router)

    if (enableSwaggerUi) {
      router
        .add[WebjarsController]
        .add(new SwaggerController(swagger = SwaggerInstance.filter(propertyClassPaths)))
    }
  }

  def configureHttpRouter(router: HttpRouter)
}
