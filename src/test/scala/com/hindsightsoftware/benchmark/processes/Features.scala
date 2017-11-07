package com.hindsightsoftware.benchmark.processes

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

object Features {
  var create = exec(session =>
      session.set("featureName", Random.alphanumeric.take(20).mkString)
  ).exec(http("Features - create")
    .post(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/features")
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("create_feature.json")).asJSON
    .check(
      jsonPath("$.id").exists.saveAs("featureId"),
    )
  )

  var addTag = exec(session =>
    session.set("featureTag", Random.alphanumeric.take(3).mkString)
  ).exec(http("Features - add tag")
    .put(session => "/rest/behavepro/2.0/project/" + session.get("projectId").as[String] + "/feature/" + session.get("featureId").as[String])
    .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .body(ElFileBody("add_tag_feature.json")).asJSON
  )

  var fetchAll = exec(http("Features - fetch all")
    .get(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/features")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
    .check(
      jsonPath("$.features[*].id").findAll.optional.saveAs("featuresIds"),
    )
  )

  var fetchAllTags = exec(http("Features - fetch all tags")
    .get(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/tags")
    .header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)
  )

  var export = exec(http("Fetures - export")
    .get(session => "/rest/behavepro/1.0/project/" + session.get("projectId").as[String] + "/feature/" + session.get("featureId").as[String] + ".feature")
    .header(HttpHeaderNames.Accept, "*/*")
  )

  var browseAll = group("Feature - browse all"){
    exec(
      fetchAll
    ).exec(
      fetchAllTags
    )
  }


}
