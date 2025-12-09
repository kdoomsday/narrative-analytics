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
import cats.effect.IO
import cats.effect.IOApp


object ClientMain extends IOApp.Simple {

  override def run: IO[Unit] = {
    given conf: ClientConfig = ConfigSource.default.loadOrThrow
    for {
      (successes, failures) <- parCall()
      _                     <- IO.println(s"Successes = $successes, Failures = $failures")
    } yield ()
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


  /**
   * Make `randomCall()`s to the server in parallel.
   * Configuration controls how many calls can be made in parallel. The *total*
   * number of calls is the number of data points desired
   *
   * @param conf `ClientConfig` to use for parameters
   * @return `IO[(Int, Int)]` with number of successes and failures
   */
  private def parCall()(using conf: ClientConfig): IO[(successes: Int, failures: Int)] =
    IO.parTraverseN(conf.maxParCalls)((1 to conf.dataPoints).toList) { _ =>
      IO.blocking(randomCall())
        .map(if _ then 1 else 0)
    }.map { l =>
        val successes = l.sum
        (successes, conf.dataPoints - successes)
    }


  /** Make a Uri out of a String */
  private inline def toUri(path: String): Uri = uri"$path"
}
