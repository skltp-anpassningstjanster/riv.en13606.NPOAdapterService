
import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.GetAlertInformationScenario
import scenarios.GetCareContactsScenario
import scenarios.GetCareDocumentationScenario
import scenarios.GetDiagnosisScenario
import scenarios.GetImagingOutcomeScenario 
import scenarios.GetLaboratoryOrderOutcomeScenario
import scenarios.GetMedicationHistoryScenario
import scenarios.GetReferralOutcomeScenario

class TP11SlowSourceSystem extends Simulation {

  val httpProtocol = http.baseURL("http://localhost:33001")
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
                    .protocols(httpProtocol)
       )
      .throttle(reachRps(maxRequestsPerSecond) in (rampSeconds seconds))
      .maxDuration(maxDuration seconds)
}