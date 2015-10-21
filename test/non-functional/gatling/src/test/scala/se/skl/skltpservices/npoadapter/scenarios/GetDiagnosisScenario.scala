package se.skl.skltpservices.npoadapter.scenarios

object GetDiagnosisScenario extends {

  val requestName     = "GetDiagnosis"
  val urn             = "urn:riv:clinicalprocess:healthcond:description:GetDiagnosisResponder:2:GetDiagnosis"
  val relativeUrl     = "getdiagnosis/v2"
  val requestFileName = "GetDiagnosisRequest.xml"
  val regex1          = "<ns2:healthcareProfessionalHSAId>SE2321000164-aen014"
  val regex2          = "Body><GetDiagnosisResponse"
  val minLength       = 4713

} with AbstractGetRequest
