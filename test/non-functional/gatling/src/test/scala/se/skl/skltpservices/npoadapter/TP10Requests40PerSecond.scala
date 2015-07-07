package se.skl.skltpservices.npoadapter;

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import se.skl.skltpservices.npoadapter.scenarios.GetAlertInformationScenario
import se.skl.skltpservices.npoadapter.scenarios.GetCareContactsScenario
import se.skl.skltpservices.npoadapter.scenarios.GetCareDocumentationScenario
import se.skl.skltpservices.npoadapter.scenarios.GetDiagnosisScenario
import se.skl.skltpservices.npoadapter.scenarios.GetImagingOutcomeScenario 
import se.skl.skltpservices.npoadapter.scenarios.GetLaboratoryOrderOutcomeScenario
import se.skl.skltpservices.npoadapter.scenarios.GetMedicationHistoryScenario
import se.skl.skltpservices.npoadapter.scenarios.GetReferralOutcomeScenario

class TP10Requests40PerSecond extends Simulation {

  val httpProtocol = http.baseURL("http://localhost:33001")
  val totalUsers:Int            = 100
  val maxRequestsPerSecond:Int  = 40
  val rampSeconds:Int           = 10
  val maxDuration:Int           = 360
    
  val getParallel = scenario("Get parallel")
                    .forever {
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
    
  setUp(getParallel.inject(rampUsers(totalUsers) over (rampSeconds seconds))
                    .protocols(httpProtocol))
      .throttle(reachRps(maxRequestsPerSecond) in (rampSeconds seconds))
      .maxDuration(maxDuration seconds)
}