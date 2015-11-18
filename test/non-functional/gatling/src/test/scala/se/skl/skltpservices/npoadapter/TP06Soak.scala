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

  // TODO - can we add assertions that check for degraded performance over time?
  
  val newUsersPerSecond:Int     = 40
  val testDuration              = 18 hours
                            //  =  7 days
    
  val getParallel = scenario("TP06Soak. Each contract - get parallel").
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
  setUp(getParallel.inject(constantUsersPerSec(newUsersPerSecond) during(testDuration)) 
                   .protocols(http.baseURL(baseURL)))
}