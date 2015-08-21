package se.skl.skltpservices.npoadapter.scenarios

object GetMedicationHistoryScenario extends {

  val requestName     = "GetMedicationHistory"
  val urn             = "urn:riv:clinicalprocess:activityprescription:actoutcome:GetMedicationHistoryResponder:1:GetMedicationHistory"
  val relativeUrl     = "getmedicationhistory/v2"
  val requestFileName = "GetMedicationHistoryRequest.xml"
  val regex1          = "id>192712079550"
  val regex2          = "Body><GetMedicationHistoryResponse"
  val length          = 12631
  
} with AbstractGetRequest
