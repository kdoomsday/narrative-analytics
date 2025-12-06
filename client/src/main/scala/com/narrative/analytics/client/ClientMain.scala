package com.narrative.analytics.client


import com.narrative.analytics.Endpoints
import com.narrative.analytics.models.Event
import pureconfig.ConfigSource
import sttp.client4.*
import sttp.model.Uri
import sttp.tapir.DecodeResult
import sttp.tapir.DecodeResult.Value
import sttp.tapir.client.sttp4.SttpClientInterpreter

import scala.util.Random


object ClientMain {

  def main(args: Array[String]): Unit = {
    given conf: ClientConfig = ConfigSource.default.loadOrThrow

    val (successes, failures) = callAndAggregate()
    println(s"Successes = $successes, Failures = $failures")
  }


  /**
   * Single call to the POST endpoint
   *
   * @param timestamp Timestamp for the call
   * @param user User id for the call
   * @param event Event for the call
   * @param conf `ClientConfig` to use. We get rootHost out of this to figure out where to call
   * @return
   */
  private def callServer(timestamp: Long, user: Long, event: Event)(using
      conf: ClientConfig
  ): Boolean = {
    val backend                                  = DefaultSyncBackend()
    val result: DecodeResult[Either[Unit, Unit]] =
      SttpClientInterpreter()
        .toClient(Endpoints.postAnalytics, Some(toUri(conf.rootHost)), backend)
        .apply((timestamp, user, event))

    result match {
      case Value(_) => true
      case _        => false
    }
  }


  /**
   * Generate random values and execute a call. Some restrictions on the values appy:
   * - timestamp goes from 0 to current epoch time
   * - user goes from 0 to `conf.maxUserId`
   *
   * @param conf `ClientConfig` with parameters
   * @return Whether the call was successful
   */
  private def randomCall()(using conf: ClientConfig): Boolean = {
    val timestamp = scala.math.abs(Random.nextLong(System.currentTimeMillis()))
    val user      = scala.math.abs(Random.nextLong(conf.maxUserId))
    val event     = Random.shuffle(Event.values).head
    callServer(timestamp, user, event)
  }


  /** Call as many times as required, and return number of successes and failures */
  private def callAndAggregate()(using conf: ClientConfig): (successes: Int, failures: Int) =
    (1 to conf.dataPoints).foldLeft((successes = 0, failures = 0)) { case (m, i) =>
      if (randomCall()) (m.successes + 1, m.failures)
      else (m.successes, m.failures)
    }


  /** Make a Uri out of a String */
  private inline def toUri(path: String): Uri = uri"$path"
}
