package com.hindsightsoftware.benchmark

import java.util.concurrent.ThreadLocalRandom

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.feeder._

class BaseSimulation extends Simulation {
  protected val conf = ConfigFactory.load("cloudformation.conf")
  protected val httpConf = http
    .baseURL(conf.getString("JIRAURL"))
    .acceptHeader("*/*")
    .userAgentHeader("Gatling")

  protected val userFeeder = csv("users_with_roles.csv").circular
  protected val searchFeeder = csv("issue_dictionary.csv").random

  // index records by project
  protected val recordsByUser: Map[String, IndexedSeq[Record[String]]] =
    csv("users_with_issues.csv").random.records.groupBy{ record => record("userKey") }

  // convert the Map values to get only the issues instead of the full records
  protected val issuesByUser: Map[String, IndexedSeq[String]] =
    recordsByUser.mapValues{ records => records.map {record => record("issueKey")} }

  protected val selectIssue = exec(session => {
    val issues = issuesByUser(session("userKey").as[String])
    val selectedIssueKey = issues(ThreadLocalRandom.current.nextInt(issues.length))
    session.set("issueKey", selectedIssueKey)
  })

  protected val selectProject = exec(session => {
    session.set("projectKey", session("issueKey").as[String].split("-")(0))
  })

  protected var selectKeyword = exec(session => {
    session.set("keyword", "")
  })
}
