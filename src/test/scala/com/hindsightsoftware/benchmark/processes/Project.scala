package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Project {
  var fetchAll = exec(http("Project - fetchAll")
    .get("/rest/api/2/project?expand=description,lead,url,projectKeys")
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var fetch = exec(http("Project - fetch")
    .get("/rest/api/2/project/" + _.get("projectKey").as[String] /*+ "?expand=description,lead,url,projectKeys"*/)
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.id").exists.saveAs("projectId")
    )
  )

  var notifications = exec(http("Project - notifications")
    .get("/rest/plugins/1.0/notifications")
    .header(HttpHeaderNames.Accept, "application/json, text/javascript, */*; q=0.01")
  )

  var fetchActivity = exec(http("Project - activity")
    .get("/projects/" + _.get("projectKey").as[String] + "?selectedItem=com.atlassian.jira.jira-projects-plugin:project-activity-summary&decorator=none&contentOnly=true")
    .header(HttpHeaderNames.Accept, "*/*")
  )

  var fetchStream = exec(http("Project - stream")
    .get("/plugins/servlet/streams?maxResults=10&relativeLinks=true&streams=key+IS+" + _.get("projectKey").as[String] + "&providers=thirdparty+dvcs-streams-provider+issues")
    .header(HttpHeaderNames.Accept, "application/xml, text/xml, */*; q=0.01")
  )

  var browse = group("Project - browse"){
    exec(http("Project - summary")
      .get("/projects/" + _.get("projectKey").as[String] + "/summary")
      .header(HttpHeaderNames.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
    ).exec(
      Resources.request("browse_project_resources_0")
    ).exec(
      Resources.request("browse_project_resources_1")
    ).exec(
      fetchActivity
    ).exec(
      Hipchat.fetch
    ).exec(
      Resources.request("browse_project_resources_2")
    ).exec(
      Resources.request("browse_project_resources_3")
    ).exec(
      fetchStream
    )
  }

  var browseAll = group("Project - browse all"){
    exec(http("browse")
      .get("/secure/BrowseProjects.jspa?selectedCategory=all&selectedProjectType=all")
      .header(HttpHeaderNames.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
    ).exec(
      Resources.request("browse_projects_resources_0")
    ).exec(
      Resources.request("browse_projects_resources_1")
    )
  }
}
