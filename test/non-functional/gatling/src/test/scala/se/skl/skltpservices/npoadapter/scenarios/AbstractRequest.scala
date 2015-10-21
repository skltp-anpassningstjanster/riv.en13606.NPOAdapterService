package se.skl.skltpservices.npoadapter.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._

trait AbstractRequest {

  def requestName:String
  def relativeUrl:String
  def urn:String
  def requestFileName:String
  def regex1:String
  def regex2:String
  def minLength:Int
  
  val headers = Map("SOAPAction" -> urn)
  
  val request = 
    exec(http(requestName)
      .post(relativeUrl)
      .headers(headers)
      .body(RawFileBody(requestFileName))
      .check(status.is(200))
      .check(bodyString.exists)
      .check(regex(regex1).exists)
      .check(regex(regex2).exists)
      .check(bodyString.not("Fault"))
    )
}