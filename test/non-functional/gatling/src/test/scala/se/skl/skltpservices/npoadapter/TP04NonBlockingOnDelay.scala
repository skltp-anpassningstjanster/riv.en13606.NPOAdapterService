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

class TP04NonBlockingOnDelay extends Simulation with HasBaseURL {

  // Send lots of requests to the adapter
  // Responses from source system are delayed, but adapter doesn't timeout
  // Assertion : Adapter continues to service requests

  // NPOAdapter-config.properties
  //   # Ange i millesekunder hur länge en ändpunkt skall vänta innan ett synkront anrop avbryts
  //   SERVICE_TIMEOUT_MS=20000
  
  val times:Int         =  3
  val pause:Int         =  0
  val simultaneousUsers = 10
  
  val getSequential = scenario("TP04NonBlockingOnDelay. Each contract - get " + times + " times sequentially")
                     .repeat(times){exec(GetAlertInformationScenario.delayedRequestWithoutTimeout)}
                     .pause(pause)
                     .repeat(times){exec(GetCareContactsScenario.delayedRequestWithoutTimeout)}
                     .pause(pause)
                     .repeat(times){exec(GetCareDocumentationScenario.delayedRequestWithoutTimeout)}
                     .pause(pause)
                     .repeat(times){exec(GetDiagnosisScenario.delayedRequestWithoutTimeout)}
                     .pause(pause)
                     .repeat(times){exec(GetImagingOutcomeScenario.delayedRequestWithoutTimeout)}
                     .pause(pause)
                     .repeat(times){exec(GetLaboratoryOrderOutcomeScenario.delayedRequestWithoutTimeout)}
                     .pause(pause)
                     .repeat(times){exec(GetMedicationHistoryScenario.delayedRequestWithoutTimeout)}
                     .pause(pause)
                     .repeat(times){exec(GetReferralOutcomeScenario.delayedRequestWithoutTimeout)}

  before {
   
// We don't want Gatling to timeout during the tests    
// Gatling timeout is set in GATLING_HOME/conf/gatling.conf
// or from the command line
// mvn gatling:execute -PTP04NonBlockingOnDelay -DbaseURL=http://ine-sit-app09.sth.basefarm.net:11006/npoadapter/ -Dgatling.http.ahc.readTimeout=70000 -Dgatling.http.ahc.connectTimeout=70000 -Dgatling.http.ahc.pooledConnectionIdleTimeout=70000 -Dgatling.http.ahc.requestTimeout=70000
//
// Unfortunately it is not possible to set timeout programmatically once the test has started.
//    
//    System.setProperty("gatling.http.ahc.requestTimeout", "10")
//    println("requestTimeout:" + System.getProperty("http.ahc:requestTimeout"))
//    System.setProperty("http.ahc.connectionTimeout"          , "70000")
//    System.setProperty("http.ahc.pooledConnectionIdleTimeout", "70000")
//    System.setProperty("http.ahc.readTimeout"                , "70000")
//    System.setProperty("http.ahc.requestTimeout"             , "70000")
//    println(System.getProperty("http.ahc.requestTimeout"))
  }
    
  setUp (getSequential.inject(atOnceUsers(simultaneousUsers)).protocols(http.baseURL(baseURL)))
}
