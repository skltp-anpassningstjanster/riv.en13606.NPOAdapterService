
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

/**
 * Exercise the Adapter at 50% CPU for a week.
 * Test for memory leaks in the Adapter JVM.
 * Test for degraded performance over time.
 */
class TP06Soak extends Simulation {

  // TODO - deduce parameters for 50% CPU
  // TODO - can we add assertions that check for degraded performance over time?
  
  val httpProtocol = http.baseURL("http://localhost:33001")
  val totalUsers:Int            = 50
  val maxRequestsPerSecond:Int  = 20
  val rampSeconds:Int           = 20
  val maxDuration:Int           = 30     // 60 * 60 * 24 * 7
    
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