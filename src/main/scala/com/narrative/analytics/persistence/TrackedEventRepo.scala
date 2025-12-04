package com.narrative.analytics.persistence


import com.narrative.analytics.models.TrackedEventCreator
import com.narrative.analytics.models.TimerangeAggregation


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
  def storeEvent(event: TrackedEventCreator): F[Unit]

  /**
   * Get a [[TimerangeAggregation]] from underlying data
   * @param startEpoch Timestamp in millis from Epoch for the start of considered data (inclusive)
   * @param endEpoch Timestamp in millis from Epoch for the end of considered data (exclusive)
   * @return [[TimerangeAggregation]] with the result
   */
  def aggregateEvents(startEpoch: Long, endEpoch: Long): F[TimerangeAggregation]
}
