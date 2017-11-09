package com.hindsightsoftware.benchmark

import io.gatling.core.Predef._
import scala.concurrent.duration._
import processes._

class BehaveProSimulation extends BaseSimulation {
  runWithPlugin = true

  setUp(
    //scn.inject(rampUsers(10) over(10 seconds))
    scn.inject(atOnceUsers(1)) // Debug purposes only
  ).protocols(httpConf)
}
