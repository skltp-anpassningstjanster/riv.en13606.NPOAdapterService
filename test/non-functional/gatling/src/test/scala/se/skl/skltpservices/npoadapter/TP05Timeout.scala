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

class TP05Timeout extends Simulation {

  // The minimum request wait is greater than the Adapter timeout

  // NPOAdapter-config.properties
  //   # Ange i millesekunder hur länge en ändpunkt skall vänta innan ett synkront anrop avbryts
  //   SERVICE_TIMEOUT_MS=20000
  
  val baseURL:String    = "http://localhost:33001"
  val times:Int         = 1
  val simultaneousUsers = 1
  val adapterTimeout    = 20000 milliseconds
  
  
  val httpProtocol = http.baseURL(baseURL)
    
  val getSequential = scenario("Get " + times + " times sequentially")
                     .exec((session) => session.set("adapterTimeoutInMilliseconds", adapterTimeout))
                     .repeat(times){exec(GetAlertInformationScenario.requestTimesout)}
                     .repeat(times){exec(GetCareContactsScenario.requestTimesout)}
                     .repeat(times){exec(GetCareDocumentationScenario.requestTimesout)}
                     .repeat(times){exec(GetDiagnosisScenario.requestTimesout)}
                     .repeat(times){exec(GetImagingOutcomeScenario.requestTimesout)}
                     .repeat(times){exec(GetLaboratoryOrderOutcomeScenario.requestTimesout)}
                     .repeat(times){exec(GetMedicationHistoryScenario.requestTimesout)}
                     .repeat(times){exec(GetReferralOutcomeScenario.requestTimesout)}
                     
  setUp (getSequential
         .inject(atOnceUsers(simultaneousUsers))
         .protocols(httpProtocol))
         .assertions(global.responseTime.max.greaterThan(adapterTimeout.toMillis.toInt), global.successfulRequests.count.lessThan(1))
}
