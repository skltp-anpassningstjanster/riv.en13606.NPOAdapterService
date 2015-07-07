package se.skl.skltpservices.npoadapter;

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import se.skl.skltpservices.npoadapter.scenarios.SendSimpleIndexScenario

class TP07SendSimpleIndex extends Simulation {

  // TODO - externalise constants
  val baseURL:String = "http://localhost:33001"
  val times:Int      = 100 // 1000000
  
  val httpProtocol = http.baseURL(baseURL)
    
  val getSequentialTimes = scenario("SendSimpleIndex " + times + " times sequentially")
                     .repeat(times){exec(SendSimpleIndexScenario.request)}
    
  setUp (getSequentialTimes.inject(atOnceUsers(1)).protocols(httpProtocol))
}