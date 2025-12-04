package com.narrative.analytics.config


import pureconfig.ConfigReader

import scala.concurrent.duration.FiniteDuration


/**
 * Base application configuration
 *
 * @param dbConfig Database connection config
 */
case class AppConfig(
    dbConfig: DBConfig,
    webConfig: WebConfig
) derives ConfigReader


/** Database configuration. Additional configurations could be placed here if needed */
case class DBConfig(
    jdbcUrl: String,
    maxPoolSize: Option[Int] = None,
    timeout: Option[FiniteDuration] = None,
    user: Option[String] = None,
    password: Option[String] = None
) derives ConfigReader


case class WebConfig(
    host: String,
    port: Int
)
