package com.narrative.analytics.service


import cats.effect.Sync
import cats.syntax.all.*
import com.narrative.analytics.models.Event
import com.narrative.analytics.models.TimeRange
import com.narrative.analytics.models.TimeRangeAggregation
import com.narrative.analytics.models.TrackedEventCreator
import com.narrative.analytics.persistence.TrackedEventRepo
import com.narrative.analytics.time.TimeRangeFinder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*


/**
 * Implementation of [[AnalyticsService]] that uses a [[TrackedEventRepo]] for
 * actual operations
 *
 * @param trackedEventRepo [[TrackedEventRepo]]
 */
class AnalyticsServiceImpl[F[_]: Sync](
    trackedEventRepo: TrackedEventRepo[F],
    timeRangeFinder: TimeRangeFinder
) extends AnalyticsService[F] {

  implicit def logger: Logger[F] = Slf4jLogger.getLogger[F]


  override def trackEvent(timestamp: Long, userId: Long, event: Event): F[Unit] =
    for {
      _ <- debug"Tracking event for userId=$userId"
      _ <- trackedEventRepo.storeEvent(TrackedEventCreator(timestamp, userId, event))
    } yield ()


  override def aggregateRange(timestamp: Long): F[TimeRangeAggregation] =
    for {
      range       <- rangeForTimestamp(timestamp)
      _           <- info"Aggregate range $range"
      aggregation <- trackedEventRepo.aggregateEvents(range)
    } yield aggregation


  /**
   * Start and end times, given a timestamp
   *
   * @param timestamp
   * @return
   */
  private inline def rangeForTimestamp(timestamp: Long): F[TimeRange] =
    Sync[F].pure(timeRangeFinder.timeRangeFor(timestamp))

}
