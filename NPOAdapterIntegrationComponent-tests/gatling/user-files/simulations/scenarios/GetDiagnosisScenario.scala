package scenarios

object GetDiagnosisScenario extends {

  val requestName     = "GetDiagnosis"
  val postUrl         = """/npoadapter/getdiagnosis/v2"""
  val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:healthcond:description:GetDiagnosisResponder:2:GetDiagnosis"""")
  val requestFileName = "GetDiagnosisRequest.xml"
  val regex1          = "id>192712049550"
  val regex2          = "Body><GetDiagnosisResponse"

} with Request
