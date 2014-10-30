
package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.config.HttpProtocolBuilder.toHttpProtocol
import io.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder

object SendSimpleIndexScenario {

  val headers = Map("""SOAPAction""" -> """"http://nationellpatientoversikt.se:SendSimpleIndex"""")

  val request = 
    exec(http("SendSimpleIndex")
      .post("""/npoadapter/npo/v1""")
      .headers(headers)
      .body(RawFileBody("SendSimpleIndexRequest.xml"))
      .check(status.is(200))
      .check(bodyString.exists)
      .check(regex("<SendSimpleIndexResponse").exists)
      .check(regex("<success>true</success>").exists)
      .check(bodyString.not("Fault"))
    )
}