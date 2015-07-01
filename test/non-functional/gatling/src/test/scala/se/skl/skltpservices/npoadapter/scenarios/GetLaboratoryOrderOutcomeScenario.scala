package se.skl.skltpservices.npoadapter.scenarios

object GetLaboratoryOrderOutcomeScenario extends {

  val requestName     = "GetLaboratoryOrderOutcome"
  val urn             = "urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcomeResponder:3:GetLaboratoryOrderOutcome"
  val relativeUrl     = "getlaboratoryorderoutcome/v3"
  val requestFileName = "GetLaboratoryOrderOutcomeRequest.xml"
  val regex1          = "id>192712069550"
  val regex2          = "Body><GetLaboratoryOrderOutcomeResponse"
  val length          = 5870
  
} with AbstractGetRequest
