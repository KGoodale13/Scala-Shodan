package com.kylegoodale.shodan

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.kylegoodale.shodan.models.HostInfo
import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}



class ShodanClient(apiKey: String)(implicit ec: ExecutionContext) {

  implicit val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }

  // TODO: Implement a throttle on the clientside that will ensure we only make 1 request per second.

  implicit val materializer = ActorMaterializer()
  private val wsClient = StandaloneAhcWSClient()


  private def getRequest[ResponseType](path: String, params: Seq[(String, String)] = Seq[(String, String)]())(implicit reads: Reads[ResponseType]):
    Future[Try[ResponseType]] =
      wsClient.url(s"$REST_API_ENDPOINT/$path")
        .addQueryStringParameters( params :+ (("key", apiKey)):_* )
        .get().map { response =>
          println(response.body)
          if(response.status != 200)
            Failure(new Exception(s"API Request to path $path failed with status code ${response.status}"))
          else
            response.body[JsValue].validate[ResponseType] match {
              case response: JsSuccess[ResponseType] => Success(response.value)
              case err: JsError => Failure(JsResultException(err.errors))
            }
        }


  /**
    * Returns all services that have been found on the given host IP.
    * @param ip - Host IP address
    * @param history - True if all historical banners should be returned (default: False)
    * @param minify - True to only return the list of ports and the general host information, no banners. (default: False)
    */
  def searchHost(ip: String, history: Boolean = false, minify: Boolean = false): Future[HostInfo] = {
    import HostInfo.hostInfoReads

    val params = Seq[(String, String)](
      ("history", history.toString),
      ("minify", minify.toString)
    )
    getRequest[HostInfo](s"/shodan/host/$ip", params).flatMap(Future.fromTry)
  }

}
