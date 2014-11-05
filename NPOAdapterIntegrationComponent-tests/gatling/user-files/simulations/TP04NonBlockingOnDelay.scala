
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

class TP04NonBlockingOnDelay extends Simulation {

  // Send lots of requests to the adapter
  // Responses from source system are delayed, but adapter doesn't timeout
  // Assertion : Adapter continues to service requests

  // NPOAdapter-config.properties
  //   # Ange i millesekunder hur länge en ändpunkt skall vänta innan ett synkront anrop avbryts
  //   SERVICE_TIMEOUT_MS=20000
  
  val baseURL:String    = "http://localhost:33001"
  val times:Int         = 2
  val pause:Int         = 0
  val simultaneousUsers = 5
  
  val httpProtocol = http.baseURL(baseURL)
    
  val getSequential = scenario("Get " + times + " times sequentially")
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
//
// Not possible to set timeout programmatically once the test has started.
//    
//    System.setProperty("gatling.http.ahc.requestTimeout", "10")
//    println("requestTimeout:" + System.getProperty("http.ahc:requestTimeout"))
//    System.setProperty("http.ahc.connectionTimeout"          , "70000")
//    System.setProperty("http.ahc.pooledConnectionIdleTimeout", "70000")
//    System.setProperty("http.ahc.readTimeout"                , "70000")
//    System.setProperty("http.ahc.requestTimeout"             , "70000")
//    println(System.getProperty("http.ahc.requestTimeout"))
  }
    
  setUp (getSequential.inject(atOnceUsers(simultaneousUsers)).protocols(httpProtocol))
}
