package se.skl.skltpservices.npoadapter.scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.config.HttpProtocolBuilder.toHttpProtocol

object PingForConfigurationScenario extends {

  val requestName     = "PingForConfiguration"
  val urn             = "http://nationellpatientoversikt.se:SendSimpleIndex"
  val requestFileName = "PingForConfiguration.xml"
  val relativeUrl     = "pfc"
  val regex1          = "<PingForConfigurationResponse"
  val regex2          = "<name>NPOService-1.1.2</name>"
  val length          = 3281
  
} with AbstractRequest