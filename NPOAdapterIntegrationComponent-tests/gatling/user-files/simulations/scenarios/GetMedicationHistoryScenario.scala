
package scenarios

object GetMedicationHistoryScenario extends {

  val requestName     = "GetMedicationHistory"
  val postUrl         = """/npoadapter/getmedicationhistory/v2"""
  val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:clinicalprocess:activityprescription:actoutcome:GetMedicationHistoryResponder:1:GetMedicationHistory"""")
  val requestFileName = "GetMedicationHistoryRequest.xml"
  val regex1          = "id>192712079550"
  val regex2          = "Body><GetMedicationHistoryResponse"
  
} with Request
