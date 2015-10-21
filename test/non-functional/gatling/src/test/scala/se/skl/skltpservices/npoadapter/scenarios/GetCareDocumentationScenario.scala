package se.skl.skltpservices.npoadapter.scenarios

object GetCareDocumentationScenario extends {

  val requestName     = "GetCareDocumentation"
  val urn             =  "urn:riv:clinicalprocess:healthcond:description:GetCareDocumentationResponder:2:GetCareDocumentation"
  val requestFileName = "GetCareDocumentationRequest.xml"
  val relativeUrl     = "getcaredocumentation/v2"
  val regex1          = "<ns2:healthcareProfessionalHSAId>SE2321000164-7381037590003ollvgiv-1"
  val regex2          = "Body><GetCareDocumentationResponse"
  val minLength       = 10000
  
} with AbstractGetRequest
