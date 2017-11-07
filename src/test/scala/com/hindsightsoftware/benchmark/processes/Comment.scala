package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

object Comment {
  def randomCommentBody() = "This is a comment #" + Random.nextInt(Integer.MAX_VALUE)

  var create = group("Comment - create") {
    exec(http("Comment - mentions")
      .get(session => "/rest/internal/2/user/mention/search?issueKey=" + session("issueKey").as[String] + "&projectKey=" + session("projectKey").as[String] + "&maxResults=10")
      .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
      .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    ).exec(_.set("comment", randomCommentBody())).exec(http("Comment - create")
      .post("/rest/api/2/issue/" + _.get("issueKey").as[String] + "/comment")
      .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
      .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
      .body(ElFileBody("create_comment.json")).asJSON
    ).exec(http("Comment - actions")
      .post("/secure/AjaxIssueAction!default.jspa")
      .header(HttpHeaderNames.ContentType, "application/x-www-form-urlencoded; charset=UTF-8")
      .header(HttpHeaderNames.Accept, "*/*")
      .formParam("issueKey", _.get("issueKey").as[String])
      .formParam("decorator", "none")
      .formParam("prefetch", "false")
      .formParam("shouldUpdateCurrentProject", "true")
    )
  }
}
