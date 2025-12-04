package com.narrative.analytics.time


import com.narrative.analytics.models.TimeRange

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


/**
 * `TimeRangeFinder` that finds the nearest hour start for a timestamp and returns
 * the range that begins there and ends at the start of the next hour
 */
class TimeRangeFinderHour() extends TimeRangeFinder {
  private val defaultZone = ZoneId.of("GMT")


  override def timeRangeFor(timestamp: Long): TimeRange = {
    val dateTime  = milli2DateTime(timestamp)
    val startTime = dateTime.truncatedTo(ChronoUnit.HOURS)
    val endTime   = startTime.plusHours(1)
    TimeRange.safe(dateTime2Milli(startTime), dateTime2Milli(endTime))
  }


  private inline def milli2DateTime(milli: Long): ZonedDateTime =
    Instant.ofEpochMilli(milli).atZone(defaultZone)


  private inline def dateTime2Milli(dt: ZonedDateTime): Long =
    dt.toInstant().toEpochMilli()

}
