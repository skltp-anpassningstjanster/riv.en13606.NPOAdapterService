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

class TP11SlowSourceSystem extends Simulation with HasBaseURL {

  val totalUsers:Int            = 10
  val maxRequestsPerSecond:Int  = 40
  val rampSeconds:Int           = 10
  val maxDuration:Int           = 360

  val getParallel = scenario("Get parallel slow")
                    .repeat(30) {
                      uniformRandomSwitch(
                        exec(GetAlertInformationScenario.slowRequest),
                        exec(GetCareContactsScenario.slowRequest),
                        exec(GetCareDocumentationScenario.slowRequest),
                        exec(GetDiagnosisScenario.slowRequest),
                        exec(GetImagingOutcomeScenario.slowRequest),
                        exec(GetLaboratoryOrderOutcomeScenario.slowRequest),
                        exec(GetMedicationHistoryScenario.slowRequest),
                        exec(GetReferralOutcomeScenario.slowRequest)
                     )
                    }
    
  setUp(getParallel.inject(rampUsers(totalUsers) over (rampSeconds seconds))
                    .protocols(http.baseURL(baseURL))
       )
      .throttle(reachRps(maxRequestsPerSecond) in (rampSeconds seconds))
      .maxDuration(maxDuration seconds)
}