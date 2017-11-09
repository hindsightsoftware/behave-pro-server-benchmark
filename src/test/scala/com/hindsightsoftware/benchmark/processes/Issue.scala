package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

object Issue {
  def randomSummary() = "Issue " + Random.nextInt(Integer.MAX_VALUE)

  var create = group("Issue - create"){
    exec(
      Resources.request("create_issue_resources_0")
    ).exec(
      Resources.request("create_issue_resources_1")
    ).exec(http("Issue - get dialog")
      .post("/secure/QuickCreateIssue!default.jspa?decorator=none")
    ).exec(
      Resources.request("create_issue_resources_2")
    ).exec(_.set("summary", randomSummary())).exec(http("Issue - create")
      .post("/rest/api/2/issue")
      .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
      .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
      .body(ElFileBody("create_issue.json")).asJSON
      .check(
        jsonPath("$.id").exists.saveAs("issueId"),
        jsonPath("$.key").exists.saveAs("issueKey")
      )
    )
  }

  var fetchAll = exec(http("Issue - fetch all")
    .get("/rest/api/2/search?jql=project=\"" + _.get("projectKey").as[String] + "\"")
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  def randomUpdateSummary() = "Updated issue " + Random.nextInt(Integer.MAX_VALUE)

  var update = exec(_.set("summary", randomUpdateSummary())).exec(http("Issue - update")
    .put("/rest/api/2/issue/" + _.get("issueKey").as[String])
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("update_issue.json")).asJSON
  )

  var editActions = exec(http("Issue - get edit actions")
    .get("/secure/AjaxIssueEditAction!default.jspa?decorator=none&issueId=" + _.get("issueId").as[String])
    .header(HttpHeaderNames.Accept, "*/*")
  )

  var getFields = exec(http("Issue - get fields") // Find issue ID
    .get("/rest/api/latest/issue/" + _.get("issueKey").as[String] + "?fields=summary,project")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.id").exists.saveAs("issueId"),
      jsonPath("$.fields.summary").exists.saveAs("summary"),
      jsonPath("$.fields.project.id").exists.saveAs("projectId"),
    )
  )

  var fetchFeatures = exec(http("Issue - features")
    .get("/rest/behavepro/1.0/project/" + _.get("projectId").as[String] + "/features")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var fetchApprovals = exec(http("Issue - approvals")
    .get(session => "/rest/behavepro/1.0/project/"  + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String])
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var fetchScenarios = exec(http("Issue - scenarios")
    .get(session => "/rest/behavepro/1.0/project/"  + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/scenarios")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var fetchTestSessions = exec(http("Issue - test sessions")
    .get(session => "/rest/behavepro/2.0/project/"  + session.get("projectId").as[String] + "/issue/" + session.get("issueId").as[String] + "/testsessions")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var browse = group("Issue - browse") {
    exec(http("Issue - browse")
      .get("/browse/" + _.get("issueKey").as[String])
      .header(HttpHeaderNames.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
    ).exec(
      Resources.request("browse_issue_resources_0")
    ).exec(
      Resources.request("browse_issue_resources_1")
    ).exec(
      Resources.request("browse_issue_resources_2")
    ).exec(
      getFields
    ).exec(
      editActions
    ).exec(
      Hipchat.fetch
    ).exec(
      Resources.request("browse_issue_resources_3")
    ).doIf(_.get("BehavePro").as[Boolean]){
      exec(
        fetchFeatures
      ).exec(
        fetchApprovals
      ).exec(
        fetchScenarios
      ).exec(
        fetchTestSessions
      )
    }
  }

  var search = exec(http("Issue - search")
    .get("/rest/api/2/search?jql=summary+%7E+%22" + _.get("searchToken").as[String] + "%22")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var searchAll = exec(http("Issue - search all")
    .get("/rest/api/2/search?jql=assignee%20in%20(currentUser())")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )
}
