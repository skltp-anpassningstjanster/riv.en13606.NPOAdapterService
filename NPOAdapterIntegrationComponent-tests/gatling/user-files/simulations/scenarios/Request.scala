package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck

trait Request {
  
  def requestName     : String
  def postUrl         : String
  def postHeaders     : Map[String, String]
  def requestFileName : String
  def regex1          : String
  def regex2          : String
  def length          : Int
//def checks          : HttpCheck
  
  val responseTimeThreshold:Long = 250 // only report response time problems for responses that take longer than this
  
  val request = exec((session) => {
                      session("careUnitHSAId").asOption[String] match {
                        case None => {
                          session.set("careUnitHSAId", "?");
                        }
                        case _ => {session}
                      }
                    }
                    )
               .exec( http(requestName) 
                     .post(postUrl)
                     .headers(postHeaders)
                     .body(ELFileBody(requestFileName))
                     .check(status.is(200))
                     .check(bodyString.exists)
                     .check(bodyString.not("Fault"))
                  // .check(bodyString.saveAs("responseBodyString"))
                     .check(bodyString.transform(w => w.length).saveAs("responseBodyStringLength"))
                     .check(bodyString.transform(s => s.length).is(length))
                     .check(regex(regex1).exists) // TODO - can we pass in a variable length list instead of two elements?
                     .check(regex(regex2).exists)
//                   .check(checks)
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

                        session("assertTimeResponseSize").asOption[Int] match {
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
  
  // response will be delayed by 10 - 20 seconds
  val slowRequest = exec((session) => {
                        session.set("careUnitHSAId", "slow")
                    })
                    .exec(request)

  // source system responds before adapter timeout
  val delayedRequestWithoutTimeout = exec((session) => {
                        session.set("careUnitHSAId", "delayWithoutTimeout")
                    })
                    .exec(request)
                    
  // verify time response proportion
  val assertTimeResponseSize = exec((session) => {
                        session.set("assertTimeResponseSize", "true")
                    })
                    .exec(request)
}