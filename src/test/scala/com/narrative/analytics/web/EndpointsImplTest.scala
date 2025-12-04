package com.narrative.analytics.web


import cats.effect.IO
import com.narrative.analytics.format.AggregationFormatterDefault
import com.narrative.analytics.service.AnalyticsService
import munit.CatsEffectSuite
import org.scalamock.stubs.Stubs
import sttp.client4.testing.BackendStub
import sttp.tapir.integ.cats.effect.CatsMonadError
import sttp.tapir.server.stub4.TapirStubInterpreter
import sttp.client4.Backend
import org.scalamock.stubs.Stub
import com.narrative.analytics.Generators
import com.narrative.analytics.models.TimeRangeAggregation
import sttp.client4.*
import sttp.model.StatusCode
import com.narrative.analytics.models.Event
import com.github.plokhotnyuk.jsoniter_scala.core.*


class EndpointsImplTest extends CatsEffectSuite, Stubs, Generators {
  type TestObjects = (backend: Backend[IO], analytics: Stub[AnalyticsService[IO]])

  private val rootPath = "http://localhost"

  test("post data successfully") {
    val to = testObjects()

    to.analytics.trackEvent.returnsWith(IO.unit)

    val (ts, userId, event) = randomTrackRequest()
    val request = sttp.client4.basicRequest
      .post(
        uri"$rootPath/analytics"
          .addParam("timestamp", ts.toString)
          .addParam("user", userId.toString)
          .addParam("event", event.toString().toLowerCase())
      )

    request.send(to.backend).map { response =>
      assertEquals(response.code, StatusCode.NoContent)

      assert(response.body.exists(_.isEmpty()), "body should be empty")

      assertEquals(to.analytics.trackEvent.times, 1)
      assertEquals(to.analytics.aggregateRange.times, 0)
      assertEquals(to.analytics.trackEvent.calls, List((ts, userId, event)))
    }
  }


  test("get aggregated data as json successfully") {
    val to = testObjects()

    val agg = randomAggregation()
    val ts = agg.timeStart + 10 // simulate a timestamp in the range
    to.analytics.aggregateRange.returnsWith(IO.pure(agg))

    val request = sttp.client4.basicRequest
      .get(
        uri"$rootPath/analyticsJson"
          .addParam("timestamp", ts.toString())
      )

    request.send(to.backend).map { response =>
      assertEquals(response.code, StatusCode.Ok)

      val aggResult =
        response.body.fold(
          _ => throw new Exception("body should be present"),
          bodyText => readFromString[TimeRangeAggregation](bodyText)
        )

      assertEquals(aggResult, agg)

      assertEquals(to.analytics.trackEvent.times, 0)
      assertEquals(to.analytics.aggregateRange.times, 1)
      assertEquals(to.analytics.trackEvent.calls, List.empty)
      assertEquals(to.analytics.aggregateRange.calls, List(ts))
    }
  }


  test("get aggregated data as formatted string successfully") {
    val to = testObjects()

    val agg = randomAggregation().copy(uniqueUsers = 1, clicks = 2, impressions = 3)
    val ts = agg.timeStart + 10 // simulate a timestamp in the range
    to.analytics.aggregateRange.returnsWith(IO.pure(agg))

    val request = sttp.client4.basicRequest
      .get(
        uri"$rootPath/analytics"
          .addParam("timestamp", ts.toString())
      )

    request.send(to.backend).map { response =>
      assertEquals(response.code, StatusCode.Ok)

      val aggResult =
        response.body.fold(
          _ => throw new Exception("body should be present"),
          identity
        )

      val expectedResult =
        """|unique_users,1
           |clicks,2
           |impressions,3""".stripMargin

      assertEquals(aggResult, expectedResult)

      assertEquals(to.analytics.trackEvent.times, 0)
      assertEquals(to.analytics.aggregateRange.times, 1)
      assertEquals(to.analytics.trackEvent.calls, List.empty)
      assertEquals(to.analytics.aggregateRange.calls, List(ts))
    }
  }


  private def testObjects(): TestObjects = {
    val service       = stub[AnalyticsService[IO]]
    val formatter     = new AggregationFormatterDefault()
    val endpointsImpl = new EndpointsImpl[IO](service, formatter)

    val backend =
      TapirStubInterpreter(BackendStub[IO](CatsMonadError[IO]))
        .whenServerEndpointsRunLogic(endpointsImpl.all)
        .backend()

    (backend, service)
  }

  /** Create a fully random set of params for timestamp, userId , and event */
  private inline def randomTrackRequest() =
    (randomLong(), randomLong(), select(Event.values))

}
