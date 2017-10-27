package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Resources {
  def request(file:String) = http(file)
    .post("/rest/webResources/1.0/resources")
    .header(HttpHeaderNames.Accept, "application/json, text/javascript, */*; q=0.01")
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody(file + ".json")).asJSON
}
