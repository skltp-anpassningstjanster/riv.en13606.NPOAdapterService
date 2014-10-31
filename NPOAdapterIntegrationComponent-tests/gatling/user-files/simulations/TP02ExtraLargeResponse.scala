
import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scenarios.GetImagingOutcomeScenario
import scenarios.GetImagingOutcomeExtraLargeScenario 

class TP02ExtraLargeResponse extends Simulation {

  // TODO - externalise constants

  val baseURL:String = "http://localhost:33001"
  val httpProtocol = http.baseURL(baseURL)
    
  val getImagingOutcome1MB = scenario("Get imaging outcome 1 MB")
                     .exec(GetImagingOutcomeScenario.request) // warm up mule with a light query
                     .exec(GetImagingOutcomeExtraLargeScenario.extraLargeResponse)
                     
  setUp (getImagingOutcome1MB.inject(atOnceUsers(1)).protocols(httpProtocol))
}