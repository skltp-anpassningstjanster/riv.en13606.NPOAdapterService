package se.skl.skltpservices.npoadapter.scenarios

object GetLaboratoryOrderOutcomeScenario extends {

  val requestName     = "GetLaboratoryOrderOutcome"
  val urn             = "urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcomeResponder:3:GetLaboratoryOrderOutcome"
  val relativeUrl     = "getlaboratoryorderoutcome/v3"
  val requestFileName = "GetLaboratoryOrderOutcomeRequest.xml"
  val regex1          = "<ns2:documentId>SE2321000164-1002Kem09042908060036603115"
  val regex2          = "Body><GetLaboratoryOrderOutcomeResponse"
  val minLength       = 5898
  
} with AbstractGetRequest
