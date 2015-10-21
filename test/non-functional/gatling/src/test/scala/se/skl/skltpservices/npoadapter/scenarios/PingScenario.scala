package se.skl.skltpservices.npoadapter.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._

trait PingScenario {

  val request = 
    exec(http("Ping")
      .post("ping")
      .check(status.is(200))
      .check(bodyString.exists)
      .check(regex("OK").exists)
    )

}