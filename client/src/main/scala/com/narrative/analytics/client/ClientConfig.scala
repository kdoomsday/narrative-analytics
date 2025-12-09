package com.narrative.analytics.client

import pureconfig.ConfigReader

case class ClientConfig(
  rootHost: String,
  dataPoints: Int,
  maxUserId: Int,
  maxParCalls: Int
) derives ConfigReader

