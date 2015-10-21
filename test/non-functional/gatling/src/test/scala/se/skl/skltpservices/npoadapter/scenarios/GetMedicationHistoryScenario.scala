package se.skl.skltpservices.npoadapter.scenarios

object GetMedicationHistoryScenario extends {

  val requestName     = "GetMedicationHistory"
  val urn             = "urn:riv:clinicalprocess:activityprescription:actoutcome:GetMedicationHistoryResponder:1:GetMedicationHistory"
  val relativeUrl     = "getmedicationhistory/v2"
  val requestFileName = "GetMedicationHistoryRequest.xml"
  val regex1          = "<ns2:documentId>SE1623210002198208149297ordination111"
  val regex2          = "Body><GetMedicationHistoryResponse"
  val minLength       = 12631
  
} with AbstractGetRequest
