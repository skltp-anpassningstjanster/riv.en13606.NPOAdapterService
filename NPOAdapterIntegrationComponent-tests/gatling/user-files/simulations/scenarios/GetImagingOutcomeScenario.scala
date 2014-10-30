package scenarios

object GetImagingOutcomeScenario extends {

  val requestName     = "GetImagingOutcome"
  val postUrl         = """/npoadapter/getimagingoutcome/v1"""
  val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcomeResponder:1:GetImagingOutcome"""")
  val requestFileName = "GetImagingOutcomeRequest.xml"
  val regex1          = "id>192712059550"
  val regex2          = "Body><GetImagingOutcomeResponse"
  
} with Request
