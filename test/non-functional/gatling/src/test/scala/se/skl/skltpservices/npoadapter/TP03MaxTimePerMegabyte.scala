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

class TP03MaxTimePerMegabyte extends Simulation with HasBaseURL {

  val times:Int    = 5
  val pause        = 1 second
  
  val httpProtocol = http.baseURL(baseURL)
    
  val getSequential = scenario("TP03MaxTimePerMegabyte. Each contract - get " + times + " times sequentially")
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
    
  setUp (getSequential.inject(atOnceUsers(10)).protocols(httpProtocol))
}