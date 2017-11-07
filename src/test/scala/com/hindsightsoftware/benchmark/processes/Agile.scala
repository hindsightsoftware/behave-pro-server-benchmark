package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Agile {
  var browseAll = group("Agile - browse all"){
    exec(http("Agile - browse")
      .get("/secure/ManageRapidViews.jspa")
      .header(HttpHeaderNames.Accept, "*/*")
    ).exec(
      Resources.request("browse_boards_resources_0")
    ).exec(
      Resources.request("browse_boards_resources_1")
    ).exec(http("Agile - view data")
      .get("/rest/greenhopper/1.0/rapidviews/viewsData")
      .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
      .check(

      )
    ).exec(
      Resources.request("browse_boards_resources_2")
    )
  }
}
