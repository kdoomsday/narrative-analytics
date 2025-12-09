package com.narrative.analytics


import cats.effect.Resource
import cats.effect.ResourceApp
import sttp.tapir.server.netty.cats.NettyCatsServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import cats.syntax.all.*
import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import com.narrative.analytics.web.EndpointsImpl
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.netty.cats.NettyCatsServerOptions
import cats.effect.std.Dispatcher
import cats.effect.syntax.dispatcher


object WebMain extends ResourceApp.Forever {
  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val module = new WebModule {}

  private val host = module.appConfig.webConfig.host
  private val port = module.appConfig.webConfig.port


  private val docEndpoints =
    SwaggerInterpreter().fromServerEndpoints(
      module.endpoints.all,
      "Narrative Analytics",
      "1.0"
    )


  override def run(args: List[String]) = {
    val prometheusMetrics = PrometheusMetrics.default[IO]()

    NettyCatsServer.io().both(Dispatcher.parallel[IO]).flatMap { (server, dispatcher) =>
      Resource
        .make(
          server
            .port(port)
            .host(host)
            .addEndpoints(module.endpoints.all)
            .addEndpoints(docEndpoints)
            .addEndpoint(prometheusMetrics.metricsEndpoint)
            .options(serverOptions(prometheusMetrics, dispatcher))
            .start()
        )(_.stop())
        .as(())
        .evalTap(_ =>
          info"Server started successfully" >>
            info"Find docs at http://$host:$port/docs"
        )
    }
  }


  private def serverOptions(prometheusMetrics: PrometheusMetrics[IO], d: Dispatcher[IO]) = {
    NettyCatsServerOptions
      .customiseInterceptors[IO](d)
      .metricsInterceptor(prometheusMetrics.metricsInterceptor())
      .options
  }

}
