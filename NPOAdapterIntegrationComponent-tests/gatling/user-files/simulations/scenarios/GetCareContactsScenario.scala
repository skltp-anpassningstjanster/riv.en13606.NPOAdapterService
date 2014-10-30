package scenarios

object GetCareContactsScenario extends {

  val requestName     = "GetCareContacts"
  val postUrl         = """/npoadapter/getcarecontacts/v2"""
  val postHeaders     = Map("""SOAPAction""" -> """"urn:riv:ehr:patientsummary:GetCareContactsResponder:2:GetCareContacts"""")
  val requestFileName = "GetCareContactsRequest.xml"
  val regex1          = "id>192712029550"
  val regex2          = "Body><GetCareContactsResponse"

} with Request
