package se.skl.skltpservices.npoadapter;

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import se.skl.skltpservices.npoadapter.scenarios.GetAlertInformationScenario
import se.skl.skltpservices.npoadapter.scenarios.GetCareContactsScenario
import se.skl.skltpservices.npoadapter.scenarios.GetCareDocumentationScenario
import se.skl.skltpservices.npoadapter.scenarios.GetDiagnosisScenario
import se.skl.skltpservices.npoadapter.scenarios.GetImagingOutcomeScenario 
import se.skl.skltpservices.npoadapter.scenarios.GetLaboratoryOrderOutcomeScenario
import se.skl.skltpservices.npoadapter.scenarios.GetMedicationHistoryScenario
import se.skl.skltpservices.npoadapter.scenarios.GetReferralOutcomeScenario

class TP01Simultaneous200Users extends Simulation {

  val httpProtocol = http.baseURL("http://localhost:33001")
  val totalUsers:Int            = 5     // 200
  val maxRequestsPerSecond:Int  = 200   // 200
  val rampDuration              = 1 seconds    // 1
  val maxDuration               = 5 minutes
  val ramp                      = 5 seconds
  
  val baseUrl:String  = if (System.getProperty("baseUrl") != null && !System.getProperty("baseUrl").isEmpty()) {
                            System.getProperty("baseUrl")
                        } else {
                            "http://localhost:33001/npoadapter/"
                        }
  
  val simultaneousRequest = scenario("Simultaneous")
                      .uniformRandomSwitch(
                        exec(GetAlertInformationScenario.request),
                        exec(GetCareContactsScenario.request),
                        exec(GetCareDocumentationScenario.request),
                        exec(GetDiagnosisScenario.request),
                        exec(GetImagingOutcomeScenario.request),
                        exec(GetLaboratoryOrderOutcomeScenario.request),
                        exec(GetMedicationHistoryScenario.request),
                        exec(GetReferralOutcomeScenario.request)
                     )
    
   setUp(simultaneousRequest.inject(rampUsers(totalUsers) over (rampDuration))
       .protocols(http.baseURL(baseUrl).disableResponseChunksDiscarding)) 
       .throttle(reachRps(maxRequestsPerSecond) in (rampDuration))
       .maxDuration(maxDuration)
}