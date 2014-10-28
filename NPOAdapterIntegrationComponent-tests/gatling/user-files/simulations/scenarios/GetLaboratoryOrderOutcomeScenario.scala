
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetLaboratoryOrderOutcomeScenario {

  val headers = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcomeResponder:3:GetLaboratoryOrderOutcome"""")

  val request = exec(http("request_0")
                    .post("""/npoadapter/getlaboratoryorderoutcome/v3""")
                    .headers(headers)
                    .body(RawFileBody("GetLaboratoryOrderOutcomeRequest.xml"))
                    .check(status.is(200))
                    .check(bodyString.exists)
                    .check(regex("id>192712069550").exists)
                    .check(regex("Body><GetLaboratoryOrderOutcomeResponse").exists)
                    .check(bodyString.not("Fault"))
                )
}