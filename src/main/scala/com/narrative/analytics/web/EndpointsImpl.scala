package com.narrative.analytics.web


import cats.effect.Sync
import cats.syntax.all.*
import com.narrative.analytics.Endpoints
import com.narrative.analytics.service.AnalyticsService
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*


/** Implements server logic for endpoints */
class EndpointsImpl[F[_]: Sync](analyticsService: AnalyticsService[F]) {
  implicit def logger: Logger[F] = Slf4jLogger.getLogger[F]

  // All endpoints exposed here
  lazy val all = List(postAnalytics, getAnalytics, getAnalyticsJson)


  val postAnalytics = Endpoints.postAnalytics.serverLogic[F] { (timestamp, userId, event) =>
    debug"Post request ($timestamp, $userId, $event)" >>
      analyticsService.trackEvent(timestamp, userId, event) >>
      Sync[F].pure(Right(()))
  }


  val getAnalytics = Endpoints.getAnalytics.serverLogic[F] { timestamp =>
    debug"Get analytics for $timestamp" >>
      analyticsService
        .aggregateRange(timestamp)
        .map(agg => Right(agg.toString()))
  }


  val getAnalyticsJson = Endpoints.getAnalyticsJson.serverLogic { timestamp =>
    analyticsService.aggregateRange(timestamp).map(Right.apply)
  }

}
