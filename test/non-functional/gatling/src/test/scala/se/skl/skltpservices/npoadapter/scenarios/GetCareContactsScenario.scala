package se.skl.skltpservices.npoadapter.scenarios

object GetCareContactsScenario extends {

  val requestName     = "GetCareContacts"
  val urn             = "urn:riv:ehr:patientsummary:GetCareContactsResponder:2:GetCareContacts"
  val requestFileName = "GetCareContactsRequest.xml"
  val relativeUrl     = "getcarecontacts/v2"
  val regex1          = "id>192712029550"
  val regex2          = "Body><GetCareContactsResponse"
  val length          = 8236

} with AbstractGetRequest
