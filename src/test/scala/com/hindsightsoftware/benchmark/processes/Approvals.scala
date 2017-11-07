package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Approvals {
  var approve = exec(http("Approvals - approve")
    .post(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/approvals")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("create_approval.json")).asJSON
  )
}
