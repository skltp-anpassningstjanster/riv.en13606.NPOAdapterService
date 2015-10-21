package se.skl.skltpservices.npoadapter.scenarios

import io.gatling.core.Predef._

object GetImagingOutcomeExtraLargeScenario extends {

  val requestName     = "GetImagingOutcome"
  val urn             = "urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcomeResponder:1:GetImagingOutcome"
  val relativeUrl     = "getimagingoutcome/v1"
  val requestFileName = "GetImagingOutcomeRequest.xml"
  val regex1          = "<ns2:documentId>OREBMKT3_9500619_3_1</ns2:documentId>"
  val regex2          = "<result><ns2:resultCode>OK</ns2:resultCode><ns2:logId>0</ns2:logId></result></GetImagingOutcomeResponse></soap:Body></soap:Envelope>"
  val minLength       = 361162

} with AbstractGetRequest {
  
  // conversion from 1 MB ehc response to RIV-TA GetImagingOutcomeResponse
  val extraLargeResponse = exec((session) => {
                               session.set("patientId", "191212120004")
                           })
                           .exec(request)
}
