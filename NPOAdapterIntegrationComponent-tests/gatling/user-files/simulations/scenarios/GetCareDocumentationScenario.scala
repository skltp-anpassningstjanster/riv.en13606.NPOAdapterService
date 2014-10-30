package scenarios

object GetCareDocumentationScenario extends {

  val requestName     = "GetCareDocumentation"
  val postUrl         = """/npoadapter/getcaredocumentation/v2"""
  val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:description:GetCareDocumentationResponder:2:GetCareDocumentation"""")
  val requestFileName = "GetCareDocumentationRequest.xml"
  val regex1          = "id>192712039550"
  val regex2          = "Body><GetCareDocumentationResponse"
  
} with Request
