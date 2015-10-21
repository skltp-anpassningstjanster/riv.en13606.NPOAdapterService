package se.skl.skltpservices.npoadapter;

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import se.skl.skltpservices.npoadapter.scenarios.GetImagingOutcomeScenario
import se.skl.skltpservices.npoadapter.scenarios.GetImagingOutcomeExtraLargeScenario 

class TP02ExtraLargeResponse extends Simulation with HasBaseURL {

  val httpProtocol = http.baseURL(baseURL).extraInfoExtractor(extraInfo => List(extraInfo.response.body))
    
  val getImagingOutcome1MB = scenario("Get imaging outcome 1 MB")
                     .exec(GetImagingOutcomeScenario.request) // warm up mule with a light query
                     .exec(GetImagingOutcomeExtraLargeScenario.extraLargeResponse)
                     
  setUp (getImagingOutcome1MB.inject(atOnceUsers(1)).protocols(httpProtocol))
}