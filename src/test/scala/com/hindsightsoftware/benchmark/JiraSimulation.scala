package com.hindsightsoftware.benchmark

import io.gatling.core.Predef._
import scala.concurrent.duration._
import processes._

class JiraSimulation extends BaseSimulation {
  val scn = scenario("JiraSimulation")
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
      .pause(1)

      // Search with random keyword from dictionary
      .repeat(2) {
        feed(searchFeeder)
        .exec(Issue.search)
        .pause(1)
      }

      // Fetch projects
      .exec(Project.browseAll)
      .pause(1)
    }

  setUp(
    scn.inject(rampUsers(10) over(10 seconds))
  ).protocols(httpConf)
}
