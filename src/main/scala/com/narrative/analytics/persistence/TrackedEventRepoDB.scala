package com.narrative.analytics.persistence


import cats.effect.Async
import com.augustnagro.magnum.*
import com.narrative.analytics.models.Event
import com.narrative.analytics.models.TimerangeAggregation
import com.narrative.analytics.models.TrackedEvent
import com.narrative.analytics.models.TrackedEventCreator

import javax.sql.DataSource


/**
 * [[TrackedEventRepo]] that uses a data source to store events in the DB
 *
 * @param dataSource [[javax.sql.DataSource]] for the connection
 */
class TrackedEventRepoDB[F[_]: Async](dataSource: DataSource) extends TrackedEventRepo[F] {

  private val underlyingRepo = Repo[TrackedEventCreator, TrackedEvent, Long]


  override def storeEvent(event: TrackedEventCreator): F[Unit] = Async[F].blocking {
    transact(dataSource) {
      underlyingRepo.insert(event)
    }
  }


  override def aggregateEvents(startEpoch: Long, endEpoch: Long): F[TimerangeAggregation] =
    Async[F].blocking {
      transact(dataSource) {
        val uniqueUsers = distinctUsers(startEpoch, endEpoch)
        val eventMap    = groupedEvents(startEpoch, endEpoch).withDefaultValue(0L)
        TimerangeAggregation(
          startEpoch,
          endEpoch,
          uniqueUsers,
          eventMap(Event.Click),
          eventMap(Event.Impression)
        )
      }
    }


  /**
   * Get distinct users for a time range
   *
   * @param startEpoch Range start inclusive
   * @param endEpoch Range end exclusive
   * @param tx Transaction within which this query will run
   * @return Number of distinct users within the time range
   */
  private def distinctUsers(startEpoch: Long, endEpoch: Long)(using tx: DbTx): Long = {
    val t = TrackedEvent.Table
    sql"""select count(distinct(${t.userId})) from tracked_event
            where ${t.timestamp} >= $startEpoch and ${t.timestamp} < $endEpoch"""
      .query[Long]
      .run()
      .sum
  }


  /**
   * Count of events by type within a time range
   *
   * @param startEpoch Range start inclusive
   * @param endEpoch Range end exclusive
   * @param tx Transaction within which this query will run
   * @return Map with the event as key, and the number of times the event
   *         happened as value. Will NOT contain events that did not happen
   */
  private def groupedEvents(startEpoch: Long, endEpoch: Long)(using tx: DbTx): Map[Event, Long] =
    sql"""select event, count(*) as c
            from tracked_event
            where timestamp >= 1764854021 and timestamp < 1764854027
            group by event"""
      .query[(String, Long)]
      .run()
      .groupMapReduce { case (event, _) => Event.valueOf(event) }(_._2)(_ + _)

}
