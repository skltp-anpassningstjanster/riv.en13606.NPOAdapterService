package se.skl.skltpservices.npoadapter.scenarios

object GetReferralOutcomeScenario extends {

  val requestName     = "GetReferralOutcome"
  val urn             = "urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcomeResponder:3:GetReferralOutcome"
  val requestFileName = "GetReferralOutcomeRequest.xml"
  val relativeUrl     = "getreferraloutcome/v3"
  val regex1          = "id>192712089550"
  val regex2          = "Body><GetReferralOutcomeResponse"
  val length          = 3281
  
} with AbstractGetRequest
