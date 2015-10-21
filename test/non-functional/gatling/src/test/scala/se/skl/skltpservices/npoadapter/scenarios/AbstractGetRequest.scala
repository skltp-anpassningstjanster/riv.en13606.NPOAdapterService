package se.skl.skltpservices.npoadapter.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck
import scala.concurrent.duration._

trait AbstractGetRequest {
  
  def requestName             : String
  def relativeUrl             : String
  def urn                     : String
  
  def requestFileName         : String
  def regex1                  : String
  def regex2                  : String
  def minLength               : Int
  
  val httpOK                  = 200
  val httpInternalServerError = 500
  
  val postHeaders = Map(
    "Accept-Encoding"                        -> "gzip,deflate",
    "Content-Type"                           -> "text/xml;charset=UTF-8",
    "x-vp-sender-id"                         -> "SE5565594230-B9P",
    "x-rivta-original-serviceconsumer-hsaid" -> "NonFunctionalTest - Gatling",
    "Keep-Alive"                             -> "115",
    "SOAPAction"                             -> urn)
  
//def checks          : HttpCheck
  
  val responseTimeThreshold:Long = 250 // only report response time problems for responses that take longer than this
  
  // requestOK
  val request = exec((session) => {
                      session("patientId").asOption[String] match {
                        case None => {
                          session.set("patientId", "191212121212"); // default patientId
                        }
                        case _ => {session}
                      }
                    }
                    )
               .exec( http(requestName) 
                     .post(relativeUrl)
                     .headers(postHeaders)
                     .body(ELFileBody(requestFileName))
                     .check(status.is(httpOK))
                     .check(bodyString.exists)
                  // .check(bodyString.saveAs("responseBodyString"))
                     .check(bodyString.transform(w => w.length).saveAs("responseBodyStringLength"))
                     .check(bodyString.transform(s => s.length).greaterThan(minLength))
                     .check(regex(regex1).exists) // TODO - can we pass in a variable length list instead of two elements?
                     .check(regex(regex2).exists)
                     .check(responseTimeInMillis.saveAs("responseTimeInMillis"))
                     
                     .check(responseTimeInMillis.lessThanOrEqual((session) => {
                       
                       // Maximum response time - one millisecond for 10000 bytes      
                       // We are not achieving this target
                       // Particularly a problem with small messages.
 
                       //  100ms <= 1 000 000 bytes / 10000
                       //    2ms <=    20 000 bytes / 10000
                       //  150ms <= 1 500 000 bytes / 10000
                       //  306ms <= 3 060 000 bytes / 10000
 
                       // Return maximum allowed milliseconds for this response

                        session("assertTimeResponseSize").asOption[Boolean] match {
                          case None => {
                            // If we are not testing for this condition, then return max int.
                            // This way, the comparison will always return true.
                            Int.MaxValue 
                          }
                          case _ => {
                            // If the response time is below the threshold, don't test 
                            if (session("responseTimeInMillis").as[Long] < responseTimeThreshold ) {
                              Int.MaxValue 
                            } else {
                              // Not more than maximum number of milliseconds per 1 megabyte response
                              ((session("responseBodyStringLength")).as[Int])/10000
                            }
                          }
                        }
                       }
                      )
                      )                        
                    // Alternative way of doing the same thing
                     /*
                     .check(bodyString.transform(s => (s.length/(1024*1024))*100).saveAs("maximumResponseTimeMilliseconds"))
                     .check(responseTimeInMillis.transform { (t:Long, session:Session) => 
                       (t < session("maximumResponseTimeMilliseconds").as[Int])
                      })
                      */
                  )

                  
  // TP03 - verify time response proportion
  val assertTimeResponseSize = exec((session) => {
                        session.set("assertTimeResponseSize", true)
                    })
                    .exec(request)
                    
  // source system responds before adapter timeout
  val delayedRequestWithoutTimeout = exec((session) => {
                        session.set("patientId", "191212120002")
                    })
                    .exec(request)
                    
  // response will be delayed by 10 - 20 seconds
  val slowRequest = exec((session) => {
                        session.set("patientId", "191212120001")
                    })
                    .exec(request)
  
  // adapter timesout before source system responds
  val requestTimesout = exec((session) => {
                        session.set("patientId", "191212120003")
                       })
                      .exec( http(requestName) 
                        .post(relativeUrl)
                        .headers(postHeaders)
                        .body(ELFileBody(requestFileName))
                        .check(status.is(httpInternalServerError))
                        .check(bodyString.exists)
                        .check(regex("Read timed out"))
                        .check(regex("faultstring"))
                        .check(responseTimeInMillis.saveAs("responseTimeInMillis"))
                        .check(responseTimeInMillis.greaterThanOrEqual((session) => {
                              (session("adapterTimeoutInMilliseconds")).as[Duration].toMillis
                       }))
                       )
                  
}