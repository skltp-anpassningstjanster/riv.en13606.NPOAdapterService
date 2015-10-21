package se.skl.skltpservices.npoadapter.scenarios

object GetAlertInformationScenario extends {
  
    val requestName     = "GetAlertInformation"
    val urn             = "urn:riv:clinicalprocess:healthcond:description:GetAlertInformationResponder:2:GetAlertInformation"
    val requestFileName = "GetAlertInformationRequest.xml"
    val relativeUrl     = "getalertinformation/v2"
    val regex1          = "<ns2:healthcareProfessionalHSAId>SE123456789012-1111"
    val regex2          = "Body><GetAlertInformationResponse"
    val minLength       = 25416
    
} with AbstractGetRequest
