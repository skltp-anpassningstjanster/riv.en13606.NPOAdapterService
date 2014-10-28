
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetReferralOutcomeScenario {

  val headers = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcomeResponder:3:GetReferralOutcome"""")

  val request = exec(http("request_0")
                    .post("""/npoadapter/getreferraloutcome/v3""")
                    .headers(headers)
                    .body(RawFileBody("GetReferralOutcomeRequest.xml"))
                    .check(status.is(200))
                    .check(bodyString.exists)
                    .check(regex("id>192712089550").exists)
                    .check(regex("Body><GetReferralOutcomeResponse").exists)
                    .check(bodyString.not("Fault"))
                )
}