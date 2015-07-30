package se.skl.skltpservices.npoadapter;

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import se.skl.skltpservices.npoadapter.scenarios.SendSimpleIndexScenario

class TP07SendSimpleIndex extends Simulation with HasBaseURL {

  val times:Int      = 10000 // 1000000
  
  val getSequentialTimes = scenario("SendSimpleIndex " + times + " times sequentially")
                     .repeat(times){exec(SendSimpleIndexScenario.request)}
    
  setUp (getSequentialTimes.inject(atOnceUsers(1)).protocols(http.baseURL(baseURL)))
}