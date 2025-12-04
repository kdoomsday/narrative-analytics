package com.narrative.analytics.persistence


import cats.effect.IO
import com.narrative.analytics.Generators
import com.narrative.analytics.models.Event
import com.narrative.analytics.models.TimeRangeAggregation
import com.narrative.analytics.models.TrackedEvent
import com.narrative.analytics.models.TrackedEventCreator
import com.narrative.analytics.time.TimeRangeFinder
import com.narrative.analytics.time.TimeRangeFinderHour
import munit.CatsEffectSuite
import org.scalamock.stubs.Stub
import org.scalamock.stubs.Stubs


class TrackedEventRepoCachedTest extends CatsEffectSuite, Stubs, Generators {
  private val timeRangeFinder = new TimeRangeFinderHour()


  test("calling to store will call underlying instance") {
    val creator          = eventCreator()
    val (instance, repo) = testObjects(creator)

    instance.storeEvent(creator).map { ev =>
        assert(repo.storeEvent.times == 1)
        assert(repo.aggregateEvents.times == 0)
        assert(ev.timestamp == creator.timestamp)
        assert(ev.event == creator.event)
        assert(ev.userId == creator.userId)
    }
  }


  test("calling to retrieve several times will only call the underlying repo once") {
    val (instance, repo) = testObjects()

    val tr = timeRangeFinder.timeRangeFor(System.currentTimeMillis())
    for {
      r1 <- instance.aggregateEvents(tr)
      r2 <- instance.aggregateEvents(tr)
    } yield {
      assert(r1 == r2)
      assert(repo.storeEvent.times == 0)
      assert(repo.aggregateEvents.times == 1)
    }
  }


  test("storing for a time range invalidates the cache for that time range") {
    val creator          = eventCreator()
    val tr               = timeRangeFinder.timeRangeFor(creator.timestamp)
    val (instance, repo) = testObjects(eventCreator())

    instance
      .storeEvent(creator)
      .flatMap(_ => instance.aggregateEvents(tr))
      .map(_ => assert(repo.aggregateEvents.times == 1))
      .flatMap(_ => instance.storeEvent(creator.copy(creator.timestamp + 100L)))
      .flatMap(_ => instance.aggregateEvents(tr))
      .map(_ => assert(repo.aggregateEvents.times == 2))
  }


  /**
   * Create new instances of test objects so values can be mocked and expectations checked
   *
   * By default will respond with a random aggregation if queried
   *
   * @param expectedCreator If present, will make underlying return an event based on it by default
   * @return A `TrackedEventRepoCached` instance with a stub repo to validate
   */
  private def testObjects(
      expectedCreator: Option[TrackedEventCreator] = None
  ): (TrackedEventRepoCached[IO], Stub[TrackedEventRepo[IO]]) = {
    val underlying = stub[TrackedEventRepo[IO]]
    val instance   = new TrackedEventRepoCached(underlying, timeRangeFinder)

    underlying.aggregateEvents.returnsWith(IO.pure(randomAggregation()))

    expectedCreator.foreach { c =>
        val eventId = randomInt()
        underlying
          .storeEvent
          .returnsWith(IO.pure(TrackedEvent(eventId, c.timestamp, c.userId, c.event)))
    }

    (instance, underlying)
  }


  // Utility to avoid maually wrapping in Some()
  private inline def testObjects(
      expectedCreator: TrackedEventCreator
  ): (TrackedEventRepoCached[IO], Stub[TrackedEventRepo[IO]]) =
    testObjects(Some(expectedCreator))

}
