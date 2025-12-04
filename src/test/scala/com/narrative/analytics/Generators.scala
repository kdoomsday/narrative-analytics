package com.narrative.analytics


import scala.util.Random
import com.narrative.analytics.models.TimeRangeAggregation
import com.narrative.analytics.models.TrackedEventCreator
import com.narrative.analytics.models.Event


/** Some helpers to generate test values */
trait Generators {

  def randomInt(min: Int = 0, max: Int = Int.MaxValue): Int =
    Random.nextInt(max - min) + min


  def randomAlpha(minLen: Int = 3, maxLen: Int = 10): String = {
    val len = randomInt(minLen, maxLen)
    Random.alphanumeric.take(len).mkString
  }


  def randomLong(min: Long = 0L, max: Long = Long.MaxValue): Long =
    Random.nextLong(max - min) + min


  /**
   * Select a random element from an iterable
   *
   * @param coll Iterable to choos from. Must not be empty
   * @return A random element from the iterable
   */
  def select[T](coll: Iterable[T]): T =
    Random.shuffle(coll).head


  /** A random event creator with params */
  def eventCreator(timestamp: Long = System.currentTimeMillis()): TrackedEventCreator =
    TrackedEventCreator(timestamp, randomLong(), select(Event.values))


  /** A `TimeRangeAggregation` with all random values */
  inline def randomAggregation() =
    TimeRangeAggregation(randomLong(), randomLong(), randomLong(), randomLong(), randomLong())

}
