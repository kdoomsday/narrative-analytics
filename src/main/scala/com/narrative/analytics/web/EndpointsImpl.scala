package com.narrative.analytics.web


import cats.effect.Sync
import cats.syntax.all.*
import com.narrative.analytics.Endpoints
import com.narrative.analytics.service.AnalyticsService


/** Implements server logic for endpoints */
class EndpointsImpl[F[_]: Sync](analyticsService: AnalyticsService[F]) {
  lazy val all = List(postAnalytics, getAnalytics, getAnalyticsJson)


  val postAnalytics = Endpoints.postAnalytics.serverLogic[F] { (timestamp, userId, event) =>
    analyticsService.trackEvent(timestamp, userId, event) >> Sync[F].pure(Right(()))
  }


  val getAnalytics = Endpoints.getAnalytics.serverLogic[F] { timestamp =>
    analyticsService.aggregateRange(timestamp).map(agg => Right(agg.toString()))
  }

  val getAnalyticsJson = Endpoints.getAnalyticsJson.serverLogic { timestamp =>
    analyticsService.aggregateRange(timestamp).map(Right.apply)
  }

}
