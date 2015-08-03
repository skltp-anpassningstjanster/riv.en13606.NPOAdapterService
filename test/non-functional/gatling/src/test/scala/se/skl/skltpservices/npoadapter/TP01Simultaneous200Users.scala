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

class TP01Simultaneous200Users extends Simulation with HasBaseURL {

  val totalUsers:Int            = 200
  val rampDuration              = 2 seconds
  val maxDuration               = 1 minutes
  
  val simultaneousRequest = scenario("Simultaneous") // .exec(GetAlertInformationScenario.request)
                      .uniformRandomSwitch(
                        exec(GetAlertInformationScenario.request),
                        exec(GetCareContactsScenario.request),
                        exec(GetCareDocumentationScenario.request),
                        exec(GetDiagnosisScenario.request),
                        exec(GetImagingOutcomeScenario.request),
                        exec(GetLaboratoryOrderOutcomeScenario.request),
                        exec(GetMedicationHistoryScenario.request),
                        exec(GetMedicationHistoryScenario.request)
                        exec(GetReferralOutcomeScenario.request),
                        exec(GetReferralOutcomeScenario.request)
                     )

   setUp(simultaneousRequest.inject(rampUsers(totalUsers) over (rampDuration))
       .protocols(http.baseURL(baseURL).disableResponseChunksDiscarding)) 
       .maxDuration(maxDuration)
}