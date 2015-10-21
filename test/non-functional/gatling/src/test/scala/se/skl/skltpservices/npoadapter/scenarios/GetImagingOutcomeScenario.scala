package se.skl.skltpservices.npoadapter.scenarios

object GetImagingOutcomeScenario extends {

  val requestName     = "GetImagingOutcome"
  val urn             =  "urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcomeResponder:1:GetImagingOutcome"
  val relativeUrl     = "getimagingoutcome/v1"
  val requestFileName = "GetImagingOutcomeRequest.xml"
  val regex1          = "<ns2:healthcareProfessionalHSAId>DIESTO"
  val regex2          = "<result><ns2:resultCode>OK</ns2:resultCode><ns2:logId>0</ns2:logId></result></GetImagingOutcomeResponse></soap:Body></soap:Envelope>"
  val minLength       = 10140

} with AbstractGetRequest
