package com.narrative.analytics.models

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import sttp.tapir.Schema


/**
 * Aggregated information for a particular time range
 * Note: Longs were chosen for the counts. We do not expect billions of anything
 * yet, but we can hope.
 *
 * @param timeStart Epoch timestamp for the start of this aggregation range
 * @param timeEnd Epoch timestamp for the end of this aggregation range
 * @param uniqueUsers Number of different users within the range
 * @param clicks Number of clicks
 * @param impressions Number of impressions
 */
case class TimeRangeAggregation(
  timeStart: Long,
  timeEnd: Long,
  uniqueUsers: Long,
  clicks: Long,
  impressions: Long
) derives ConfiguredJsonValueCodec,
      Schema
