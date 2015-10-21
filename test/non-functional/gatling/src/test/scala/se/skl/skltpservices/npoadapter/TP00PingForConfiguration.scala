package se.skl.skltpservices.npoadapter;

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import se.skl.skltpservices.npoadapter.scenarios.PingScenario
import se.skl.skltpservices.npoadapter.scenarios.PingForConfigurationScenario

class TP00PingForConfiguration extends Simulation with HasBaseURL {

  val times:Int = 2
  val getSequentialTimes = scenario("Ping, PingForConfiguration")
     .exec(http("Ping")
      .get("ping")
      .check(status.is(200))
      .check(bodyString.exists)
      .check(regex("OK").exists)
      )
      .exec(PingForConfigurationScenario.request)
  
  setUp (getSequentialTimes.inject(atOnceUsers(1)).protocols(http.baseURL(baseURL)))
}