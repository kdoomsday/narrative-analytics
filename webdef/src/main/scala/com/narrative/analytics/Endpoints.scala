package com.narrative.analytics


import com.narrative.analytics.models.Event
import com.narrative.analytics.models.TimeRangeAggregation
import com.narrative.analytics.models.Event.given
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.jsoniter.*

import scala.annotation.meta.param


object Endpoints {

  val postAnalytics = endpoint
    .post
    .in("analytics")
    .in(query[Long]("timestamp"))
    .in(query[Long]("user"))
    .in(query[Event]("event"))
    .out(statusCode(StatusCode.NoContent))


  val getAnalytics = endpoint
    .get
    .in("analytics")
    .in(query[Long]("timestamp"))
    .out(stringBody)


  val getAnalyticsJson = endpoint
    .get
    .in("analyticsJson")
    .in(query[Long]("timestamp"))
    .out(jsonBody[TimeRangeAggregation])

}
