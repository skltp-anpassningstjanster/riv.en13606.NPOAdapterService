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

class TP08Sequential extends Simulation with HasBaseURL {

  val times:Int    = 10   // 1000
  val pause        =  2 seconds
  
  val httpProtocol = http.baseURL(baseURL)
    
  val getSequentialTimes = scenario("TP08Sequential. Each contract - get " + times + " times sequentially")
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
    
  setUp (getSequentialTimes.inject(atOnceUsers(1)).protocols(http.baseURL(baseURL)))
}