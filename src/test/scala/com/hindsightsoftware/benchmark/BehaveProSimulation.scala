package com.hindsightsoftware.benchmark

import io.gatling.core.Predef._
import scala.concurrent.duration._
import processes._

class BehaveProSimulation extends BaseSimulation {
  runWithPlugin = true

  setUp(
    scn.inject(rampUsers(100) over(600 seconds))
  ).protocols(httpConf).maxDuration(30 minutes)
}
