package com.hindsightsoftware.benchmark

import java.text.SimpleDateFormat
import java.util.concurrent.ThreadLocalRandom

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.feeder._
import io.gatling.core.Predef._
import scala.util.Random

import scala.concurrent.duration._
import processes._

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

  protected var runWithPlugin = false

  protected val scn = scenario("Simulation")
    .exec{session => session.set("BehavePro", runWithPlugin)}
    .exec{session =>
      var formatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
      val timestamp: Long = System.currentTimeMillis / 1000
      session.set("timestamp", formatUTC.format(timestamp))
    }
    .feed(userFeeder)

    // Login
    .exec(Login.login)
    .pause(1)

    // Master counter
    .repeat(5, "globalIndex") {

      // View dashboard counter
      repeat(2, "dashboardIndex"){
        exec(Dashboard.browse)
        .pause(1)

        // View issue index
        .repeat(5, "issueIndex") {
          // Randomly select issue to view by active user
          exec(selectIssue)

          // View issue
          .exec(Issue.browse)
          .pause(1)
        }
      }

      // Randomly select issue and set project key
      .exec(selectIssue).exec(selectProject)

      // Fetch project to extract project ID from key
      .exec(Project.fetch)
      .exec(Project.browse)
      .pause(1)

      // Create a new issue using the project ID
      .exec(Issue.create)
      .exec(Issue.browse)
      .pause(1)

      // Then comment on the new issue
      .exec(Comment.create)
      // Then update the new issue
      .exec(Issue.update)
      // Then post a question
      .doIf(_.get("BehavePro").as[Boolean]) {
        exec(Questions.create)
        .pause(1)
        .exec(Questions.resolve)
        .pause(1)
        .exec(Approvals.approve)
        .pause(1)
        .exec(Features.browseAll)
        .exec(Features.create)
        .repeat(3){
          exec(Features.addTag)
          .exec(Features.fetchAll) // For every tag creation there is a fetch!
        }
        .pause(1)
        // Create 3 scenarios and update 5 times each
        .repeat(3){
          exec(Scenarios.create)
          .exec(Scenarios.fetchAll)
          .repeat(3){
            exec(Scenarios.addTag)
            .pause(1)
            .exec(Scenarios.update)
          }
          .pause(1)
          .exec(Scenarios.link)
          .exec(Scenarios.fetch)
        }

        // Randomly select feature id and browse all scenarios
        .repeat(5){
          exec(session => {
            val featuresList = session("featuresIds").as[Vector[Any]].toList
            val featureId = featuresList(Random.nextInt(featuresList.size))
            session.set("featureId", featureId)
          })
          .exec(Scenarios.fetchAll)
          .pause(1)
        }
      }

      // Search with random keyword from dictionary
      .repeat(2) {
        feed(searchFeeder)
        .exec(Issue.search)
        .pause(1)
      }

      // Fetch projects
      .exec(Project.browseAll)
      .pause(1)

      // Browse boards
      .exec(Agile.browseAll)
    }

    // Fetch agile boards
    .exec(Agile.browseAll)
}
