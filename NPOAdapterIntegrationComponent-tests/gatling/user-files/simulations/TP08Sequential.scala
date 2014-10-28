
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

class TP08Sequential extends Simulation {

  // TODO - externalise constants

  val baseURL:String = "http://localhost:33001"
  val times:Int      = 1000
  val pause:Int      = 5
  
  val httpProtocol = http.baseURL(baseURL)
    
  val getSequentialTimes = scenario("Get " + times + " times sequentially")
                     .repeat(times){exec(GetAlertInformationScenario.request)}
                     .pause(pause)
                     .repeat(times){exec(GetCareContactsScenario.request)}
                     .pause(pause)
                     .repeat(times){exec(GetCareDocumentationScenario.request)}
                     .pause(pause)
                     .repeat(times){exec(GetDiagnosisScenario.request)}
                     .pause(pause)
                     .repeat(times){exec(GetImagingOutcomeScenario.request)}
                     .pause(pause)
                     .repeat(times){exec(GetLaboratoryOrderOutcomeScenario.request)}
                     .pause(pause)
                     .repeat(times){exec(GetMedicationHistoryScenario.request)}
                     .pause(pause)
                     .repeat(times){exec(GetReferralOutcomeScenario.request)}
    
  setUp (getSequentialTimes.inject(atOnceUsers(1)).protocols(httpProtocol))
}