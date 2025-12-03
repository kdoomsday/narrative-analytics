package com.narrative.analytics

import cats.effect.Async

class EndpointsImpl[F[_]: Async]() {
  lazy val all = List(postAnalytics)

  val postAnalytics = Endpoints.postAnalytics.serverLogic[F] { (timestamp, userId, event) =>
    Async[F].pure(Right(()))
  }
}

