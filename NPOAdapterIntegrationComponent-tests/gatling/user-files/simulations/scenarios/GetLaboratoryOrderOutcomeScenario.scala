
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object GetLaboratoryOrderOutcomeScenario extends {

    val requestName     = "GetLaboratoryOrderOutcome"
    val postUrl         = """/npoadapter/getlaboratoryorderoutcome/v3"""
    val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcomeResponder:3:GetLaboratoryOrderOutcome"""")
    val requestFileName = "GetLaboratoryOrderOutcomeRequest.xml"
    val regex1          = "id>192712069550"
    val regex2          = "Body><GetLaboratoryOrderOutcomeResponse"
  
} with Request
