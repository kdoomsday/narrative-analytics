package com.narrative.analytics


import cats.effect.IO
import com.narrative.analytics.config.AppConfig
import com.narrative.analytics.persistence.TrackedEventRepo
import com.narrative.analytics.persistence.TrackedEventRepoDB
import com.narrative.analytics.service.AnalyticsService
import com.narrative.analytics.service.AnalyticsServiceImpl
import com.narrative.analytics.time.TimeRangeFinder
import com.narrative.analytics.time.TimeRangeFinderHour
import com.narrative.analytics.web.EndpointsImpl
import com.zaxxer.hikari.HikariDataSource
import pureconfig.ConfigSource

import javax.sql.DataSource
import com.narrative.analytics.persistence.TrackedEventRepoCached


trait WebModule {
  import com.softwaremill.macwire._

  val appConfig: AppConfig = ConfigSource.default.loadOrThrow


  val dataSource: DataSource = {
    val dataSource: HikariDataSource = new HikariDataSource()
    dataSource.setJdbcUrl(appConfig.dbConfig.jdbcUrl)

    appConfig.dbConfig.maxPoolSize.foreach(size => dataSource.setMaximumPoolSize(size))
    appConfig.dbConfig.timeout.foreach(timeout => dataSource.setConnectionTimeout(timeout.toMillis))
    appConfig.dbConfig.user.foreach(u => dataSource.setUsername(u))
    appConfig.dbConfig.password.foreach(p => dataSource.setPassword(p))

    dataSource
  }


  lazy val timeRangeFinder: TimeRangeFinder       = wireRec[TimeRangeFinderHour]

  // Need to use a specific instance here so the correct underlying implementation is used
  lazy val trackedEventRepo: TrackedEventRepo[IO] = new TrackedEventRepoCached[IO](wireRec[TrackedEventRepoDB[IO]], timeRangeFinder)

  lazy val analyticsService: AnalyticsService[IO] = wireRec[AnalyticsServiceImpl[IO]]
  lazy val endpoints: EndpointsImpl[IO]           = wireRec[EndpointsImpl[IO]]
}
