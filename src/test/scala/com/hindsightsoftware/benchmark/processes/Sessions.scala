package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Sessions {
  var fetchBoards = exec(http("Sessions - fetch boards")
    .get(session => "/rest/agile/1.0/board?projectKeyOrId=" + session.get("projectKey").as[String])
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.values[*].id").findAll.optional.saveAs("boardIds"),
      jsonPath("$.values[0].id").find.optional.saveAs("boardId")
    )
  ).exec(session => {
    println("found board ID: " + session.get("boardId").as[String])
    session
  })

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
