package se.skl.skltpservices.npoadapter.scenarios

object PingForConfigurationScenario extends {

  val requestName     = "PingForConfiguration"
  val urn             = "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1"
  val requestFileName = "PingForConfiguration.xml"
  val relativeUrl     = "pfc"
  val regex1          = "<PingForConfigurationResponse"
  val regex2          = "<name>INBOUND_HOST_HTTP</name>"
  val minLength       = 3281
  
} with AbstractRequest