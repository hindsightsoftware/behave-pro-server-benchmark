package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

object Sessions {
  var fetchBoards = exec(http("Sessions - fetch boards")
    .get(session => "/rest/agile/1.0/board?projectKeyOrId=" + session.get("projectKey").as[String])
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.values[*].id").findAll.optional.saveAs("boardIds"),
      jsonPath("$.values[0].id").find.optional.saveAs("boardId")
    )
  )

  var fetchSprint = exec(http("Sessions - fetch sprint")
    .get(session => "/rest/agile/1.0/board/" + session.get("boardId").as[String] + "/sprint")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.values").exists,
      jsonPath("$.values[*].id").findAll.optional.saveAs("sprintIds")
    )
  )

  var fetchSprintIssues = exec(http("Sessions - fetch sprint issues")
    .get(session => "/rest/agile/1.0/board/" + session.get("boardId").as[String] + "/sprint/" + session.get("sprintId").as[String] + "/issue?jql=")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.issues[*].id").findAll.optional.saveAs("sprintIssueIds")
    )
  )

  var fetchSessions = exec(http("Sessions - fetch for issue")
    .get(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.estSessions[*].sessionId").findAll.optional.saveAs("sessionIds")
    )
  )

  var fetch = exec(http("Sessions - fetch")
    .get(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/testsessions/" + session.get("sessionUrl").as[String].split("/").last)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.issue").exists.saveAs("issueId")
    )
  )

  var fetchReport = exec(http("Sessions - fetch report")
    .get(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions/" + session.get("sessionUrl").as[String].split("/").last + "/report")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var fetchActivities = exec(http("Sessions - fetch activities")
    .get(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions/" + session.get("sessionUrl").as[String].split("/").last + "/activities")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var fetchIssue = exec(http("Sessions - fetch issue")
    .get(session => "/rest/api/2/issue/" + session.get("issueId").as[String])
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var start = exec(http("Sessions - start")
    .put(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions/" + session.get("sessionUrl").as[String].split("/").last + "/start")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("empty.json")).asJSON
  )

  var pause = exec(http("Sessions - pause")
    .put(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions/" + session.get("sessionUrl").as[String].split("/").last + "/pause")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("empty.json")).asJSON
  )

  var resume = exec(http("Sessions - resume")
    .put(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions/" + session.get("sessionUrl").as[String].split("/").last + "/resume")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("empty.json")).asJSON
  )

  var update = exec(session =>
    session.set("sessionName", Random.alphanumeric.take(20).mkString)
  ).exec(http("Sessions - update")
    .put(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions/" + session.get("sessionUrl").as[String].split("/").last)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("update_test_session.json")).asJSON
  )

  var browse = group("Sessions - browse"){
    exec(
      fetch
    ).exec(
      fetchReport
    ).exec(
      fetchActivities
    ).exec(
      fetchIssue
    )
  }

  var create = group("Sessions - create"){
    exec(session =>
      session.set("sessionName", Random.alphanumeric.take(20).mkString)
    ).exec(http("Sessions - create")
      .post(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions")
      .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
      .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
      .body(ElFileBody("create_test_session.json")).asJSON
      .check(
        header("Location").exists.saveAs("sessionUrl")
      )
    ).exec {
      fetchSessions
    }
  }

  // If this throws "NoSuchElementException: key not found" you can blame Gatling
  // session.set() magically does not always work...
  // Reason: "it only works in some specific places" - Gatling documentation
  var browseAll = group("Sessions - browse all"){
    exec(
      Resources.request("browse_test_sessions_resources_0")
    ).exec(
      Resources.request("browse_test_sessions_resources_1")
    ).exec(
      Resources.request("browse_test_sessions_resources_2")
    ).exec(
      fetchBoards
    ).doIf(_.contains("boardId")){
      fetchSprint
    }.doIf(_.contains("sprintIds")){
      exec(session => session.set("sprintId", session.get("sprintIds").as[Vector[String]].last))
    }.doIf(_.contains("sprintId")){
      fetchSprintIssues
    }.foreach("${sprintIssueIds}", "ssueId"){
      fetchSessions
    }
  }
}
