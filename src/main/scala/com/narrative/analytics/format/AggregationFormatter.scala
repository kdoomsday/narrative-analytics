package com.narrative.analytics.format

import com.narrative.analytics.models.TimeRangeAggregation

trait AggregationFormatter {
  def format(agg: TimeRangeAggregation): String
}

