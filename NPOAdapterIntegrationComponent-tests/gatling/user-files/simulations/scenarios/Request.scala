package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._

trait Request {
  
  def requestName     : String
  def postUrl         : String
  def postHeaders     : Map[String, String]
  def requestFileName : String
  def regex1          : String
  def regex2          : String
  
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
                     .check(regex(regex1).exists) // TODO - can we pass in a variable length list instead of two elements?
                     .check(regex(regex2).exists)
                    )
  
  // response will be delayed by 10 - 20 seconds
  val slowRequest = exec((session) => {
                        session.set("careUnitHSAId", "slow")
                    })
                    .exec(request)
}