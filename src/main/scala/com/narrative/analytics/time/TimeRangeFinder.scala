package com.narrative.analytics.time

import com.narrative.analytics.models.TimeRange

trait TimeRangeFinder {
  def timeRangeFor(timestamp: Long): TimeRange
}

