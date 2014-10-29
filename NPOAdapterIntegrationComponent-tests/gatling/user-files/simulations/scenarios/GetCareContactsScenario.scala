
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetCareContactsScenario {

  val headers = Map("""SOAPAction""" -> """"urn:riv:ehr:patientsummary:GetCareContactsResponder:2:GetCareContacts"""")

  val request = exec(http("request_0")
                    .post("""/npoadapter/getcarecontacts/v2""")
                    .headers(headers)
                    .body(RawFileBody("GetCareContactsRequest.xml"))
                    .check(status.is(200))
                    .check(bodyString.exists)
                    .check(regex("id>192712029550").exists)
                    .check(regex("Body><GetCareContactsResponse").exists)
                    .check(bodyString.not("Fault"))
         )
}