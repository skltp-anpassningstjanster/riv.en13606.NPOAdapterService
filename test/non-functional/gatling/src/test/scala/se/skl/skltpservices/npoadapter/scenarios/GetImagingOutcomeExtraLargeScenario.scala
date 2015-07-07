package se.skl.skltpservices.npoadapter.scenarios

import io.gatling.core.Predef._

object GetImagingOutcomeExtraLargeScenario extends {

  val requestName     = "GetImagingOutcome"
  val urn             = "urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcomeResponder:1:GetImagingOutcome"
  val relativeUrl     = "getimagingoutcome/v1"
  val requestFileName = "GetImagingOutcomeRequest.xml"
  val regex1          = "id>192712319550"
  val regex2          = "<result><ns2:resultCode>OK</ns2:resultCode><ns2:logId>0</ns2:logId></result></GetImagingOutcomeResponse></soap:Body></soap:Envelope>"
  val length          = 361162

} with AbstractGetRequest {
  
  // conversion from 1 MB ehc response to RIV-TA GetImagingOutcomeResponse
  val extraLargeResponse = exec((session) => {
                               session.set("careUnitHSAId", "ExtraLarge")
                           })
                           .exec(request)
}
