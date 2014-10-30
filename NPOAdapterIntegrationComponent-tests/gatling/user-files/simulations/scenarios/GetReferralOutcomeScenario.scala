
package scenarios

object GetReferralOutcomeScenario extends {

  val requestName     = "GetReferralOutcome"
  val postUrl         = """/npoadapter/getreferraloutcome/v3"""
  val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcomeResponder:3:GetReferralOutcome"""")
  val requestFileName = "GetReferralOutcomeRequest.xml"
  val regex1          = "id>192712089550"
  val regex2          = "Body><GetReferralOutcomeResponse"
  
} with Request
