package com.narrative.analytics.persistence


import cats.effect.Sync
import cats.syntax.all.*
import com.github.blemale.scaffeine.Cache
import com.github.blemale.scaffeine.Scaffeine
import com.narrative.analytics.models.TimeRange
import com.narrative.analytics.models.TimeRangeAggregation
import com.narrative.analytics.models.TrackedEvent
import com.narrative.analytics.models.TrackedEventCreator
import com.narrative.analytics.time.TimeRangeFinder

import scala.concurrent.duration.DurationInt


/**
 * [[TrackedEventRepo]] that caches results and invalidates the cache when a
 * new record is inserted.
 *
 * Caching this way ensures reads are faster, as we know there are many more
 * reads than writes.
 *
 * Uses an underlying repo for actual reading and writing via the Decorator
 * pattern
 *
 * @param underlying [[TrackedEventRepo]] to use for actual storage
 */
class TrackedEventRepoCached[F[_]: Sync](
    underlying: TrackedEventRepo[F],
    timeRangeFinder: TimeRangeFinder
) extends TrackedEventRepo[F] {

  private val cache: Cache[TimeRange, TimeRangeAggregation] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(5.minutes)
      .maximumSize(100)
      .build[TimeRange, TimeRangeAggregation]()


  override def storeEvent(event: TrackedEventCreator): F[TrackedEvent] =
    Sync[F].blocking(cache.invalidate(timeRangeFinder.timeRangeFor(event.timestamp))) >> underlying
      .storeEvent(event)


  override def aggregateEvents(range: TimeRange): F[TimeRangeAggregation] =
    cache
      .getIfPresent(range)
      .map(Sync[F].pure)
      .getOrElse {
        for {
          agg <- underlying.aggregateEvents(range)
          _   <- Sync[F].blocking(cache.put(range, agg))
        } yield agg
      }

}
