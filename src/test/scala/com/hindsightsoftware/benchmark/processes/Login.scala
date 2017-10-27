package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Login {
  // System.getProperty("url")
  var login = exec(http("Login - login")
    .post("/rest/auth/1/session")
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .body(StringBody(session => "{\"username\":\"" + session("userKey").as[String] + "\",\"password\":\"" + session("userPassword").as[String] + "\"}"))
    .check(
      jsonPath("$..session.name").exists.saveAs("authSessionName"),
      jsonPath("$..session.value").exists.saveAs("authSessionValue")
    )
  )
}