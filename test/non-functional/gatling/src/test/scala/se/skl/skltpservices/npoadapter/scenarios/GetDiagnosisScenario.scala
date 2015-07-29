package se.skl.skltpservices.npoadapter.scenarios

object GetDiagnosisScenario extends {

  val requestName     = "GetDiagnosis"
  val urn             = "urn:riv:clinicalprocess:healthcond:description:GetDiagnosisResponder:2:GetDiagnosis"
  val relativeUrl     = "getdiagnosis/v2"
  val requestFileName = "GetDiagnosisRequest.xml"
  val regex1          = "id>192712049550"
  val regex2          = "Body><GetDiagnosisResponse"
  val length          = 4713
  val baseUrl         = if (System.getProperty("baseUrl") != null && !System.getProperty("baseUrl").isEmpty()) {
                            System.getProperty("baseUrl")
                        } else {
                            "http://localhost:33001/npoadapter/getdiagnosis/v2"
                        }

} with AbstractGetRequest
