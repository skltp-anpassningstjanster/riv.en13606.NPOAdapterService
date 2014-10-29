
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetCareDocumentationScenario {

  val headers = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:description:GetCareDocumentationResponder:2:GetCareDocumentation"""")

  val request = exec(http("request_0")
                    .post("""/npoadapter/getcaredocumentation/v2""")
                    .headers(headers)
                    .body(RawFileBody("GetCareDocumentationRequest.xml"))
                    .check(status.is(200))
                    .check(bodyString.exists)
                    .check(regex("id>192712039550").exists)
                    .check(regex("Body><GetCareDocumentationResponse").exists)
                    .check(bodyString.not("Fault"))
                    )
}