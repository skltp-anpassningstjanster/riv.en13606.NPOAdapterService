
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetMedicationHistoryScenario {

  val headers = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:activityprescription:actoutcome:GetMedicationHistoryResponder:1:GetMedicationHistory"""")

  val request = exec(http("request_0")
                    .post("""/npoadapter/getmedicationhistory/v2""")
                    .headers(headers)
                    .body(RawFileBody("GetMedicationHistoryRequest.xml"))
                    .check(status.is(200))
                    .check(bodyString.exists)
                    .check(regex("id>192712079550").exists)
                    .check(regex("Body><GetMedicationHistoryResponse").exists)
                    .check(bodyString.not("Fault"))
                  )
}