package com.hindsightsoftware.benchmark

import io.gatling.core.Predef._
import scala.concurrent.duration._
import processes._

class BehaveProSimulation extends BaseSimulation {
  runWithPlugin = true

  setUp(
    scn.inject(rampUsers(1) over(5 minutes))
  ).protocols(httpConf).maxDuration(10 minutes)
}
