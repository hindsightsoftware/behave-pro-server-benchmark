package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

object Scenarios {
  var fetchAll = exec(http("Scenarios - fetch all")
    .get(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/feature/" + session.get("featureId").as[String] + "/scenarios")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var fetch = exec(http("Scenarios - fetch")
    .get(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/feature/" + session.get("featureId").as[String] + "/scenario/" + session.get("scenarioId").as[String])
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.revision").exists.saveAs("scenarioRevision")
    )
  )

  var create = exec(session =>
    session
      .set("scenarioName", Random.alphanumeric.take(20).mkString)
      .set("scenarioSteps", "Given " + Random.alphanumeric.take(20).mkString)
  ).exec(http("Scenarios - create")
    .post(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/feature/" + session.get("featureId").as[String] + "/scenarios")
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("create_scenario.json")).asJSON
    .check(
      jsonPath("$.id").exists.saveAs("scenarioId"),
      jsonPath("$.revision").exists.saveAs("scenarioRevision")
    )
  )

  var addTag = exec(session =>
    session
      .set("scenarioTag", Random.alphanumeric.take(3).mkString)
      .set("scenarioRevision", (session.get("scenarioRevision").as[String].toInt + 1).toString)
  ).exec(http("Scenarios - add tag")
    .put(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/feature/" + session.get("featureId").as[String] + "/scenario/" + session.get("scenarioId").as[String])
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .header("If-Match", _.get("scenarioRevision").as[String])
    .body(ElFileBody("add_tag_scenario.json")).asJSON
    .check(
      jsonPath("$.revision").exists.saveAs("scenarioRevision")
    )
  )

  var update = exec(session =>
    session
      .set("scenarioName", Random.alphanumeric.take(20).mkString)
      .set("scenarioSteps", "Given " + Random.alphanumeric.take(20).mkString)
      .set("scenarioRevision", (session.get("scenarioRevision").as[String].toInt + 1).toString)
  ).exec(http("Scenarios - update")
    .put(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/feature/" + session.get("featureId").as[String] + "/scenario/" + session.get("scenarioId").as[String])
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .header("If-Match", _.get("scenarioRevision").as[String])
    .body(ElFileBody("update_scenario.json")).asJSON
    .check(
      jsonPath("$.revision").exists.saveAs("scenarioRevision")
    )
  )

  var link = exec(session =>
    session
      .set("projectIdInt", session.get("projectId").as[String].toInt)
      .set("featureIdInt", session.get("featureId").as[String].toInt)
      .set("scenarioIdInt", session.get("scenarioId").as[String].toInt)
  ).exec(http("Scenarios - create link to issue")
    .post(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/features/" + session.get("featureId").as[String] + "/scenarios/" + session.get("scenarioId").as[String])
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("link_scenario_issue.json")).asJSON
  ).exec(http("Scenarios - fetch linked scenarios")
    .get(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String])
  )
}
