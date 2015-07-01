package se.skl.skltpservices.npoadapter.scenarios

object GetAlertInformationScenario extends {
  
    val requestName     = "GetAlertInformation"
    val urn             = "urn:riv:clinicalprocess:healthcond:description:GetAlertInformationResponder:2:GetAlertInformation"
    val requestFileName = "GetAlertInformationRequest.xml"
    val relativeUrl     = "getalertinformation/v2"
    val regex1          = "id>192712019550"
    val regex2          = "Body><GetAlertInformationResponse"
    val length          = 25840
    
} with AbstractGetRequest
