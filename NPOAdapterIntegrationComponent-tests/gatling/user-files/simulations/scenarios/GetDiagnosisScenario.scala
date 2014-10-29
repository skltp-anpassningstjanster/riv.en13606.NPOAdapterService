
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetDiagnosisScenario {

  val headers = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:description:GetDiagnosisResponder:2:GetDiagnosis"""")

  val request = exec(http("request_0")
                     .post("""/npoadapter/getdiagnosis/v2""")
                     .headers(headers)
                     .body(RawFileBody("GetDiagnosisRequest.xml"))
                     .check(status.is(200))
                     .check(bodyString.exists)
                     .check(regex("id>192712049550").exists)
                     .check(regex("Body><GetDiagnosisResponse").exists)
                     .check(bodyString.not("Fault"))
                    )
}