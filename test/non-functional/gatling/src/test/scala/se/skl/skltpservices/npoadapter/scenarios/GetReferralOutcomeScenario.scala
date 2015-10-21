package se.skl.skltpservices.npoadapter.scenarios

object GetReferralOutcomeScenario extends {

  val requestName     = "GetReferralOutcome"
  val urn             = "urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcomeResponder:3:GetReferralOutcome"
  val requestFileName = "GetReferralOutcomeRequest.xml"
  val relativeUrl     = "getreferraloutcome/v3"
  val regex1          = "<ns2:documentId>OREBMKT3_9871961_3_1"
  val regex2          = "Body><GetReferralOutcomeResponse"
  val minLength       = 3271
  
} with AbstractGetRequest
