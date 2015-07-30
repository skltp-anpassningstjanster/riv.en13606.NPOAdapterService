package se.skl.skltpservices.npoadapter;

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import se.skl.skltpservices.npoadapter.scenarios.PingForConfigurationScenario

class TP00PingForConfiguration extends Simulation with HasBaseURL {

  val times:Int = 2
  val getSequentialTimes = scenario("PingForConfiguration " + times + " times sequentially")
                                   .repeat(times){exec(PingForConfigurationScenario.request)}

  setUp (getSequentialTimes.inject(atOnceUsers(1)).protocols(http.baseURL(baseURL)))
}