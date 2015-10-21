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

/**
 * Exercise the Adapter at 50% CPU for a week.
 * Test for memory leaks in the Adapter JVM.
 * Test for degraded performance over time.
 */
class TP06Soak extends Simulation with HasBaseURL {

  // TODO - deduce parameters for 50% CPU
  // TODO - can we add assertions that check for degraded performance over time?
  
  val totalUsers:Int            =  1
  val maxRequestsPerSecond:Int  =  10
  val ramp                      =  1 minute
  val testDuration              =  1 minutes
  val maxDuration               =  1 minute
                            //  =  7 days
    
  val getParallel = scenario("TP06Soak. Each contract - get parallel")
                    .repeat(100000) {
                      uniformRandomSwitch(
                        exec(GetAlertInformationScenario.request),
                        exec(GetCareContactsScenario.request),
                        exec(GetCareDocumentationScenario.request),
                        exec(GetDiagnosisScenario.request),
                        exec(GetImagingOutcomeScenario.request),
                        exec(GetLaboratoryOrderOutcomeScenario.request),
                        exec(GetMedicationHistoryScenario.request),
                        exec(GetReferralOutcomeScenario.request)
                     )
                    }
/*    
  setUp(getParallel.inject(constantUsersPerSec(3) during(testDuration)) 
                   .protocols(http.baseURL(baseURL)))
                   .maxDuration(maxDuration)
*/
  setUp(getParallel.inject(atOnceUsers(3)) 
                   .protocols(http.baseURL(baseURL)))
                   .maxDuration(maxDuration)
}