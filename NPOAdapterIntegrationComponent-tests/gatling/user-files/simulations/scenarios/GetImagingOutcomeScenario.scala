package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object GetImagingOutcomeScenario extends {

  val requestName     = "GetImagingOutcome"
  val postUrl         = """/npoadapter/getimagingoutcome/v1"""
  val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcomeResponder:1:GetImagingOutcome"""")
  val requestFileName = "GetImagingOutcomeRequest.xml"
  val regex1          = "id>192712059550"
  val regex2          = "<result><ns2:resultCode>OK</ns2:resultCode><ns2:logId>0</ns2:logId></result></GetImagingOutcomeResponse></soap:Body></soap:Envelope>"
  var length          = 10862

} with Request