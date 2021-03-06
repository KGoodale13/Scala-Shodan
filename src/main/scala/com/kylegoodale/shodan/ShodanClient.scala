package com.kylegoodale.shodan

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.kylegoodale.shodan.models._
import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSResponse
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedSimpleForm

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

  // Implicit Option[T] formatter for json body parsing optional values. Source: https://stackoverflow.com/questions/43031412/no-json-formatter-for-optionstring
  private implicit def optionFormat[T: Format]: Format[Option[T]] = new Format[Option[T]]{
    override def reads(json: JsValue): JsResult[Option[T]] = json.validateOpt[T]

    override def writes(o: Option[T]): JsValue = o match {
      case Some(t) ⇒ implicitly[Writes[T]].writes(t)
      case None ⇒ JsNull
    }
  }


  private def handleResponse[ResponseType](response: StandaloneWSResponse)
    (implicit reads: Reads[ResponseType]): Try[ResponseType] =
  {
    if(response.status != 200)
      Failure(new Exception(s"API Request to path ${response.uri.getPath} failed with status code ${response.status}. Error Message: ${response.body}"))
    else
      response.body[JsValue].validate[ResponseType] match {
        case response: JsSuccess[ResponseType] => Success(response.value)
        case err: JsError => Failure(JsResultException(err.errors))
      }
  }

  private def postRequest[ResponseType](path: String, params: Map[String, String] = Map[String, String]())
    (implicit reads: Reads[ResponseType]): Future[Try[ResponseType]] =
      wsClient.url(s"$REST_API_ENDPOINT$path")
        .addQueryStringParameters(("key", apiKey))
        .post(params)
        .map(handleResponse[ResponseType](_))

  private def getRequest[ResponseType](path: String, params: Seq[(String, String)] = Seq[(String, String)]())
    (implicit reads: Reads[ResponseType]): Future[Try[ResponseType]] =
      wsClient.url(s"$REST_API_ENDPOINT/$path")
        .addQueryStringParameters( params :+ (("key", apiKey)):_* )
        .get()
        .map(handleResponse[ResponseType](_))


  /**
    * Returns information about the Shodan account linked to this API key.
    * @return Profile - This accounts profile data
    */
  def profile(): Future[Profile] = getRequest[Profile]("/account/profile").flatMap(Future.fromTry)

  /** Search methods **/

  /**
    * Returns all services that have been found on the given host IP.
    * @param ip - Host IP address
    * @param history - True if all historical banners should be returned (default: False)
    * @param minify - True to only return the list of ports and the general host information, no banners. (default: False)
    */
  def hostInfo(ip: String, history: Boolean = false, minify: Boolean = false): Future[HostInfo] = {
    import HostInfo.hostInfoReads

    val params = Seq[(String, String)](
      ("history", history.toString),
      ("minify", minify.toString)
    )
    getRequest[HostInfo](s"/shodan/host/$ip", params).flatMap(Future.fromTry)
  }

  /**
    * Searches Shodan using the passed search query
    * @param query - A query string using the same syntax as the website
    * @param facets - A facet string to retrieve stats on the matching data set i.e top 10 countries matching the query (Default: None)
    * @param page - The page number (Default: 1)
    * @param minify - if true this will truncate some of the larger data fields. (Default true)
    * @return
    */
  def hostSearch(query: String, facets: Option[String] = None, page: Int = 1, minify: Boolean = true): Future[HostSearchResult] = {
    import HostSearchResult._

    val params = Seq[(String, String)](
      ("query", query),
      ("page", page.toString),
      ("minify", minify.toString)
    ) ++ facets.map(("facets", _))
    getRequest[HostSearchResult]("/shodan/host/search", params).flatMap(Future.fromTry)
  }

  /**
    * Searches Shodan using the passed search query and returns only the total count of matches and any facet information.
    * @param query - A query string using the same syntax as the website
    * @param facets - A facet string to retrieve stats on the matching data set i.e top 10 countries matching the query (Default: None)
    * @return HostSearchResult containing only the total count and facet info
    */
  def hostCount(query: String, facets: Option[String] = None): Future[HostSearchResult] = {
    import HostSearchResult._

    val params = Seq[(String, String)](
      ("query", query)
    ) ++ facets.map(("facets", _))
    getRequest[HostSearchResult]("/shodan/host/count", params).flatMap(Future.fromTry)
  }

  /**
    * This method lets you determine which filters are being used by the query string and what parameters were provided to the filters.
    * @param query - A query string using the same syntax as the website
    * @return SearchTokenResult containing information on how Shodan interpreted the query string as described above
    */
  def hostSearchTokens(query: String): Future[SearchTokenResult] = {
    val params = Seq[(String, String)](
      ("query", query)
    )
    getRequest[SearchTokenResult]("/shodan/host/search/tokens", params).flatMap(Future.fromTry)
  }

  /**
    * This method returns a list of port numbers that the crawlers are looking for.
    * @return List[Int]
    */
  def ports(): Future[List[Int]] = getRequest[List[Int]]("/shodan/ports").flatMap(Future.fromTry)


  /** On-Demand scanning methods **/


  /**
    * This method returns an object containing all the protocols that can be used when launching an Internet scan.
    * @return
    */
  def protocols(): Future[Map[String, String]] = getRequest[Map[String, String]]("/shodan/protocols").flatMap(Future.fromTry)

  /**
    * Use this method to request Shodan to crawl a network.
    * @param ips A comma-separated list of IPs or netblocks (in CIDR notation) that should get crawled.
    * @return ScanResponse
    */
  def scan(ips: String): Future[ScanResponse] =
    postRequest[ScanResponse]("/shodan/scan", Map("ips" -> ips)).flatMap(Future.fromTry)

  // Overloaded method to allow ips to be passed as varargs for convenience
  def scan(ips: String*): Future[ScanResponse] = scan(ips.mkString(","))

  /**
    * Check the progress of a previously submitted scan request
    * @param scanId The unique scan ID that was returned by /shodan/scan
    * @return ScanStatus
    */
  def scanStatus(scanId: String): Future[ScanStatus] = getRequest[ScanStatus](s"/shodan/scan/$scanId").flatMap(Future.fromTry)


  /** Bulk Data methods **/


  /**
    * Use this method to see a list of the datasets that are available for download.
    * @return List[Dataset]
    */
  def datasets(): Future[List[Dataset]] = getRequest[List[Dataset]]("/shodan/data").flatMap(Future.fromTry)

  /**
    * Get a list of files that are available for download from the provided dataset.
    * @param datasetName - Name of the dataset to retrieve a list of available files for
    * @return A list of DatasetFiles containing info on available files for download in the queried dataset
    */
  def datasetFiles(datasetName: String) = getRequest[List[DatasetFile]](s"/shodan/data/$datasetName").flatMap(Future.fromTry)



  /** DNS methods **/


  /**
    * Look up the IP address for the provided list of hostnames.
    * @param hostnames vararg list of hostnames to retrieve ip addresses for
    * @return Map of hostname -> ip address
    */
  def resolveDNS(hostnames: String*) =
    getRequest[Map[String, Option[String]]]("/dns/resolve", Seq(("hostnames", hostnames.mkString(",")))).flatMap(Future.fromTry)


  /**
    * Look up the hostnames that have been defined for the given list of IP addresses.
    * @param ips vararg list of ips to retrieve ip addresses for
    * @return Map of ip address -> list of hostnames associations
    */
  def reverseDNS(ips: String*) =
    getRequest[Map[String, Option[List[String]]]]("/dns/reverse", Seq(("ips", ips.mkString(",")))).flatMap(Future.fromTry)


  /** Utility methods **/


  /**
    * Shows the HTTP headers that your client sends when connecting to a webserver.
    * @return Map of key value pairs of headers
    */
  def httpHeaders() = getRequest[Map[String, String]]("/tools/httpheaders").flatMap(Future.fromTry)

  /**
    * Get your current IP address as seen from the Internet.
    * @return Your IP address (String)
    */
  def myIP() = getRequest[String]("/tools/myip").flatMap(Future.fromTry)


  /**
    * Returns information about the API plan belonging to the given API key.
    * @return APIInfo containing information on this accounts remaining credits and api plan
    */
  def getAPIInfo() = getRequest[APIInfo]("/api-info").flatMap(Future.fromTry)


  /**
    * Calculates a honeypot probability score ranging from 0 (not a honeypot) to 1.0 (is a honeypot).
    * @param ip - The IP to calculate the honeypot score for
    * @return The score from 0->1 as a double
    */
  def honeyScore(ip: String) =
    wsClient.url(s"$REST_API_ENDPOINT/labs/honeyscore/$ip")
    .addQueryStringParameters(("key", apiKey))
    .get()
    .map { response =>
      if(response.status != 200)
        Failure(new Exception(s"API Request to path ${response.uri.getPath} failed with status code ${response.status}. Error Message: ${response.body}"))
      else
        Success(response.body.toDouble)
    }.flatMap(Future.fromTry)
}
