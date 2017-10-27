package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Dashboard {
  var fetchStream = exec(http("Dashboard - stream")
    .get("/plugins/servlet/streams?maxResults=5&relativeLinks=true")
    .header(HttpHeaderNames.Accept, "*/*")
  )

  var browse = group("Dashboard - browse") {
    exec(http("Dashboard - browse")
      .get("/rest/gadget/1.0/issueTable/jql?num=10&tableContext=jira.table.cols.dashboard&addDefault=true&enableSorting=true&paging=true&showActions=true&jql=assignee+%3D+currentUser()+ORDER+BY+priority+DESC%2C+created+ASC&sortBy=&startIndex=" + _.get("dashboardIndex").as[String])
      .header(HttpHeaderNames.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
    ).exec(
      Resources.request("dashboard_resources_0")
    ).exec(
      Resources.request("dashboard_resources_1")
    ).exec(
      fetchStream
    ).exec(
      Resources.request("dashboard_resources_2")
    )
  }
}
