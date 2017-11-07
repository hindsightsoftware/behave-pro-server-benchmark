package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Questions {
  var create = group("Questions - create"){
    exec(http("Questions - submit")
      .post(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/questions")
      .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
      .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
      .body(ElFileBody("create_question.json")).asJSON
    ).exec(http("Questions - fetch")
      .get(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String])
      .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
      .check(
        jsonPath("$.questions[0].questionId").exists.saveAs("questionId")
      )
    )
  }

  var resolve = exec(http("Questions - approve")
    .put(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/questions/" + session.get("questionId").as[String])
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("resolve_question.json")).asJSON
  )
}
