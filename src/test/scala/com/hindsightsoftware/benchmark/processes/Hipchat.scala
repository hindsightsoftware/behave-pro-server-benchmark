package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Hipchat {
  var fetch = exec(http("Hipchat - fetch")
    .get("/rest/hipchat/integrations/1.0/issuepanel/data/" + _.get("issueKey").as[String])
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )
}
