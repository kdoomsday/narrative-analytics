package com.narrative.analytics.persistence


import com.narrative.analytics.models.TrackedEventCreator
import com.narrative.analytics.models.TimeRange
import com.narrative.analytics.models.TimeRangeAggregation
import com.narrative.analytics.models.TrackedEvent


/**
 * Repository to track and query event info
 */
trait TrackedEventRepo[F[_]] {

  /**
   * Store an event
   *
   * @param event [[TrackedEventCreator]] with the event's info
   * @return Created [[TrackedEvent]]
   */
  def storeEvent(event: TrackedEventCreator): F[TrackedEvent]

  /**
   * Get a [[TimerangeAggregation]] from underlying data
   * @param range [[TimeRange]] for which we query
   * @return [[TimerangeAggregation]] with the result
   */
  def aggregateEvents(range: TimeRange): F[TimeRangeAggregation]
}
