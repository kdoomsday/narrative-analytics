package com.narrative.analytics.models

/**
 * A time range in [startEpoch, endEpoch)
 *
 * @param startEpoch Start of the range (inclusive)
 * @param endEpoch End of the range (exclusive)
 */
case class TimeRange(startEpoch: Long, endEpoch: Long) {
  assert(startEpoch <= endEpoch)
}


object TimeRange {

  /**
   * Construct a new TimeRange instance, ensuring that `startEpoch <= endEpoch`.
   * If this does not hold it will switch the start and end and still construct the instance
   *
   * @return A `TimeRange` where we are sure `startEpoch <= endEpoch`
   */
  def safe(startEpoch: Long, endEpoch: Long) =
    if (startEpoch <= endEpoch) new TimeRange(startEpoch, endEpoch)
    else new TimeRange(endEpoch, startEpoch)

}
