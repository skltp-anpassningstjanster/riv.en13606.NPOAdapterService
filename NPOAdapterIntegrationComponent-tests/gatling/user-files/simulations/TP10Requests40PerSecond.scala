
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

class TP10Requests40PerSecond extends Simulation {

  val httpProtocol = http.baseURL("http://localhost:33001")
  val totalUsers:Int  = 40
  val rampSeconds:Int = 10
  val maxDuration:Int = 360
    
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
      .throttle(reachRps(totalUsers) in (rampSeconds seconds))
      .maxDuration(maxDuration seconds)
}