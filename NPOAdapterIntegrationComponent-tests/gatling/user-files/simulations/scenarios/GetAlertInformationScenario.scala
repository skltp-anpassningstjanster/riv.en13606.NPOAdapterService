
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetAlertInformationScenario {

  val headers = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:description:GetAlertInformationResponder:2:GetAlertInformation"""")

  val request = exec(http("request_0")
                     .post("""/npoadapter/getalertinformation/v2""")
                     .headers(headers)
                     .body(RawFileBody("GetAlertInformationRequest.xml"))
                     .check(status.is(200))
                     .check(bodyString.exists)
                     .check(regex("id>192712019550").exists)
                     .check(regex("Body><GetAlertInformationResponse").exists)
                     .check(bodyString.not("Fault"))
     )
}