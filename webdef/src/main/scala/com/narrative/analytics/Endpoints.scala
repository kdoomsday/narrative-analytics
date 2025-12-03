package com.narrative.analytics


import sttp.tapir.*
import sttp.tapir.json.jsoniter.*
import scala.annotation.meta.param
import com.narrative.analytics.models.Event
import com.narrative.analytics.models.Event.given
import sttp.model.StatusCode


object Endpoints {

  val postAnalytics = endpoint
    .post
    .in("analytics")
    .in(query[Long]("timestamp"))
    .in(query[Long]("user"))
    .in(query[Event]("event"))
    .out(statusCode(StatusCode.NoContent))

}
