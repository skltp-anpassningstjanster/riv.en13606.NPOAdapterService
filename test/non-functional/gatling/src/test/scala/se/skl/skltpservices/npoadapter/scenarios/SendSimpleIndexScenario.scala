package se.skl.skltpservices.npoadapter.scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.config.HttpProtocolBuilder.toHttpProtocol

object SendSimpleIndexScenario extends {

  val requestName     = "SendSimpleIndex"
  val urn             = "http://nationellpatientoversikt.se:SendSimpleIndex"
  val relativeUrl     = "npo/v1"
  val requestFileName = "SendSimpleIndexRequest.xml"
  val regex1          = "<SendSimpleIndexResponse"
  val regex2          = "<success>true</success>"
  val length          = 3281
  
} with AbstractRequest