package com.narrative.analytics.service


import com.narrative.analytics.models.Event
import com.narrative.analytics.models.TimerangeAggregation
import com.narrative.analytics.models.TrackedEventCreator


/**
 * Service to register and query information about tracked events
 */
trait AnalyticsService[F[_]] {

  /**
   * Register an event
   *
   * @param timestamp Event timestamp expressed as millis since epoch
   * @param userId User identifier
   * @param event Type of event
   * @return Successful F if no issues
   */
  def trackEvent(timestamp: Long, userId: Long, event: Event): F[Unit]

  /**
   * Get the [[TimerangeAggregation]] around a specific timestamp
   *
   * @param timestamp Timestamp that is being queried
   * @return [[TimerangeAggregation]] for that timestamp
   */
  def aggregateRange(timestamp: Long): F[TimerangeAggregation]
}
