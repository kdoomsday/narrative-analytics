package com.narrative.analytics.format

import com.narrative.analytics.models.TimeRangeAggregation


/**
 * `AggregationFormatter` that renders into the following format:
 * {{{
 *   unique_users,{number_of_unique_usernames}
 *   clicks,{number_of_clicks}
 *   impressions,{number_of_impressions}
 * }}}
 */
class AggregationFormatterDefault extends AggregationFormatter {

  override def format(agg: TimeRangeAggregation): String =
    s"""|unique_users,${agg.uniqueUsers}
        |clicks,${agg.clicks}
        |impressions,${agg.impressions}""".stripMargin

}
