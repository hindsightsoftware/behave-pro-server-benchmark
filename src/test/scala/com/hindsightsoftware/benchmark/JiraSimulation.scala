package com.hindsightsoftware.benchmark

import io.gatling.core.Predef._
import scala.concurrent.duration._
import processes._

class JiraSimulation extends BaseSimulation {
  runWithPlugin = false

  setUp(
    scn.inject(rampUsers(10) over(10 seconds))
  ).protocols(httpConf)
}
