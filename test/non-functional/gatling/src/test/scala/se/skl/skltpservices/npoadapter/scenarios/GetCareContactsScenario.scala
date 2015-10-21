package se.skl.skltpservices.npoadapter.scenarios

object GetCareContactsScenario extends {

  val requestName     = "GetCareContacts"
  val urn             = "urn:riv:ehr:patientsummary:GetCareContactsResponder:2:GetCareContacts"
  val requestFileName = "GetCareContactsRequest.xml"
  val relativeUrl     = "getcarecontacts/v2"
  val regex1          = "<ns2:orgUnitHSAId>SE2321000164-7381037590003"
  val regex2          = "Body><GetCareContactsResponse"
  val minLength       = 7500

} with AbstractGetRequest
