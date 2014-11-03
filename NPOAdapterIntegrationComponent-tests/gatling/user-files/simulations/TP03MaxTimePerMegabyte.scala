
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

class TP03MaxTimePerMegabyte extends Simulation {

  // TODO - externalise constants

  val baseURL:String = "http://localhost:33001"
  val times:Int      = 3
  val pause:Int      = 1
  
  val httpProtocol = http.baseURL(baseURL)
    
  val getSequential = scenario("Get " + times + " times sequentially")
                     .repeat(times){exec(GetAlertInformationScenario.assertTimeResponseSize)}
                     .pause(pause)
                     .repeat(times){exec(GetCareContactsScenario.assertTimeResponseSize)}
                     .pause(pause)
                     .repeat(times){exec(GetCareDocumentationScenario.assertTimeResponseSize)}
                     .pause(pause)
                     .repeat(times){exec(GetDiagnosisScenario.assertTimeResponseSize)}
                     .pause(pause)
                     .repeat(times){exec(GetImagingOutcomeScenario.assertTimeResponseSize)}
                     .pause(pause)
                     .repeat(times){exec(GetLaboratoryOrderOutcomeScenario.assertTimeResponseSize)}
                     .pause(pause)
                     .repeat(times){exec(GetMedicationHistoryScenario.assertTimeResponseSize)}
                     .pause(pause)
                     .repeat(times){exec(GetReferralOutcomeScenario.assertTimeResponseSize)}
    
  setUp (getSequential.inject(atOnceUsers(1)).protocols(httpProtocol))
}