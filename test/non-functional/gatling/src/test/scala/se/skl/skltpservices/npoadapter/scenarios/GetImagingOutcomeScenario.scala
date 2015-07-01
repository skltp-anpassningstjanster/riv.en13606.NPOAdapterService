package se.skl.skltpservices.npoadapter.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object GetImagingOutcomeScenario extends {

  val requestName     = "GetImagingOutcome"
  val urn             =  "urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcomeResponder:1:GetImagingOutcome"
  val relativeUrl     = "getimagingoutcome/v1"
  val requestFileName = "GetImagingOutcomeRequest.xml"
  val regex1          = "id>192712059550"
  val regex2          = "<result><ns2:resultCode>OK</ns2:resultCode><ns2:logId>0</ns2:logId></result></GetImagingOutcomeResponse></soap:Body></soap:Envelope>"
  val length          = 10862

} with AbstractGetRequest
