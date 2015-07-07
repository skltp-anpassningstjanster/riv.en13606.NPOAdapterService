package se.skl.skltpservices.npoadapter.scenarios

object GetCareDocumentationScenario extends {

  val requestName     = "GetCareDocumentation"
  val urn             =  "urn:riv:clinicalprocess:healthcond:description:GetCareDocumentationResponder:2:GetCareDocumentation"
  val requestFileName = "GetCareDocumentationRequest.xml"
  val relativeUrl     = "getcaredocumentation/v2"
  val regex1          = "id>192712039550"
  val regex2          = "Body><GetCareDocumentationResponse"
  val length          = 13211
  
} with AbstractGetRequest
