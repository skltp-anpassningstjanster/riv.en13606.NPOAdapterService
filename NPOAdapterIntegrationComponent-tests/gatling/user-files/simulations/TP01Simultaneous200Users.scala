
import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scenarios.GetAlertInformationScenario
import scenarios.GetCareContactsScenario
import scenarios.GetCareDocumentationScenario
import scenarios.GetDiagnosisScenario
import scenarios.GetImagingOutcomeScenario 
import scenarios.GetLaboratoryOrderOutcomeScenario
import scenarios.GetMedicationHistoryScenario
import scenarios.GetReferralOutcomeScenario

class TP01Simultaneous200Users extends Simulation {

  val httpProtocol = http.baseURL("http://localhost:33001")
  val totalUsers:Int            = 10    // 200
  val maxRequestsPerSecond:Int  = 200   // 200
  val rampSeconds:Int           = 1     // 1
  val maxDuration:Int           = 300   // 300
    
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
    
  setUp(simultaneousRequest.inject(rampUsers(totalUsers) over (rampSeconds seconds))
                    .protocols(httpProtocol))
      .throttle(reachRps(maxRequestsPerSecond) in (rampSeconds seconds))
      .maxDuration(maxDuration seconds)
}