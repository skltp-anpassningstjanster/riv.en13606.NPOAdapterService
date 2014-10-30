package scenarios

object GetAlertInformationScenario extends {
  
    val requestName     = "GetAlertInformation"
    val postUrl         = """/npoadapter/getalertinformation/v2"""
    val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:description:GetAlertInformationResponder:2:GetAlertInformation"""")
    val requestFileName = "GetAlertInformationRequest.xml"
    val regex1          = "id>192712019550"
    val regex2          = "Body><GetAlertInformationResponse"
    
} with Request
