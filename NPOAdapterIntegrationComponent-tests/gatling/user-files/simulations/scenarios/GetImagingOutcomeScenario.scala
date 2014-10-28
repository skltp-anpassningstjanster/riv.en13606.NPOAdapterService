
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetImagingOutcomeScenario {

  val headers = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcomeResponder:1:GetImagingOutcome"""")

  val request = exec(http("request_0")
                    .post("""/npoadapter/getimagingoutcome/v1""")
                    .headers(headers)
                    .body(RawFileBody("GetImagingOutcomeRequest.xml"))
                    .check(status.is(200))
                    .check(bodyString.exists)
                    .check(regex("id>192712059550").exists)
                    .check(regex("Body><GetImagingOutcomeResponse").exists)
                    .check(bodyString.not("Fault"))
                )
}