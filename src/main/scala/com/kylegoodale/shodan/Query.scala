package com.kylegoodale.shodan

import java.text.SimpleDateFormat
import java.util.Calendar


object Query {
  // The format shodan expects dates to be in when querying
  private val dateTimeFormat = new SimpleDateFormat("dd/MM/YYYY")
}

/**
  * A builder for easily creating search filters for querying the shodan api.
  * Contains helper functions for setting the different filter values
  */
class Query(
  private[Query] val filters: Map[String, String] = Map[String, String](),
  private[Query] val bannerSearch: String = "" // Any raw strings will be searched for in the devices banners
) {

  /**
    * Compiles the query filters into a single query string formatted to Shodan's API query specs
    * @return String of comma separated filter:value pairs
    */
  def result: String = s"$bannerSearch " + filters.map(_.productIterator.mkString(":")).mkString(" ")

  private def copy(filters: Map[String, String] = filters, bannerSearch: String = bannerSearch) = new Query(filters, bannerSearch)

  // Helper function for adding filters and returning a new Query instance with those filters
  private def addFilter(filterKey: String, value: String): Query = this.copy(filters = this.filters + (filterKey -> value))

  /** Filter functions. Can be changed together to create a query with multiple filters **/

  def bannerContains(string: String) = this.copy(bannerSearch = this.bannerSearch + s"$string ")

  def collectedAfter(date: Calendar) = addFilter("after", Query.dateTimeFormat.format(date))
  def collectedBefore(date: Calendar) = addFilter("before", Query.dateTimeFormat.format(date))

  def withASN(asn: String) = addFilter("asn", asn)

  def withCity(city: String) = addFilter("city", city)
  def withCountry(country: String) = addFilter("country", country)
  def withState(state: String) = addFilter("state", state)
  def withPostalCode(postalCode: String) = addFilter("postal", postalCode)
  def withPostalCode(postalCode: Int): Query = this.withPostalCode(postalCode.toString)
  def withinGeoRadius(latitude: Double, longitude: Double, radius: Double) =
    addFilter("geo", s"$latitude,$longitude,$radius")
  def withinGeoBoundingBox(minLatitude: Double, minLongitude: Double, maxLatitude: Double, maxLongitude: Double) =
    addFilter("geo", s"$minLatitude,$minLongitude,$maxLatitude,$maxLongitude")

  def withHostname(hostname: String) = addFilter("hostname", hostname)
  def withIPv6 = addFilter("has_ipv6", "true")
  def withoutIPv6 = addFilter("has_ipv6", "false")
  def withinNetCIDRBlock(networkBlock: String) = addFilter("net", networkBlock)
  def withPort(port: String) = addFilter("port", port)
  def withPort(port: Int): Query = this.withPort(port.toString)


  def withScreenshot = addFilter("has_screenshot", "true")
  def withoutScreenshot = addFilter("has_screenshot", "false")

  def withISP(isp: String) = addFilter("isp", isp)
  def withOrg(org: String) = addFilter("org", org)
  def withOS(os: String) = addFilter("os", os)

  def withDataHash(hash: String) = addFilter("hash", hash)
  def withProduct(productName: String) = addFilter("product", productName)
  def withVersion(version: String) = addFilter("version", version)

  object link {
    val ETHERNET_OR_MODEM = apply("Ethernet or modem")
    val GENERIC_TUNNEL_OR_VPN = apply("generic tunnel or VPN")
    val DSL = apply("DSL")
    val IPIP_OR_SIT = apply("IPIP or SIT")
    val SLIP = apply("SLIP")
    val IPSEC_OR_GRE = apply("IPSec or GRE")
    val VLAN = apply("VLAN")
    val JUMBO_ETHERNET = apply("jumbo Ethernet")
    val GOOGLE = apply("Google")
    val GIF = apply("GIF")
    val PPTP = apply("PPTP")
    val LOOPBACK = apply("loopback")
    val AX25_RADIO_MODEM = apply("AX.25 radio modem")
    def apply(link: String) = addFilter("link", link)
  }

  object withBitcoin {
    def peerIP(ip: String) = addFilter("bitcoin.ip", ip)

    def ipCount(count: Int) = addFilter("bitcoin.ip_count", count.toString)

    def port(port: String) = addFilter("bitcoin.port", port)
    def port(port: Int): Query = this.port(port.toString)
  }

  object withTelnet {
    def option(optionName: String) = addFilter("telnet.option", optionName)
    def `do`(optionName: String) = addFilter("telnet.do", optionName)
    def dont(optionName: String) = addFilter("telnet.dont", optionName)
    def will(optionName: String) = addFilter("telnet.will", optionName)
    def wont(optionName: String) = addFilter("telnet.wont", optionName)
  }

  object withNTP {
    def ip(ip: String) = addFilter("ntp.ip", ip)
    def ipCount(count: Int) = addFilter("ntp.ip_count", count.toString)
    def port(port: String) = addFilter("ntp.port", port)
    def port(port: Int): Query = this.port(port.toString)
    def moreIPsAvailable = addFilter("ntp.more", "true")
    def noMoreIPsAvailable = addFilter("ntp.more", "false")
  }

  object withSSL {
    def applicationProtocol(protocol: String) = addFilter("ssl.alpn", protocol)
    def chainCount(count: Int) = addFilter("ssl.chain_count", count.toString)

    def cipherVersion(version: String) = addFilter("ssl.cipher.version", version)
    def cipherBits(bits: String) = addFilter("ssl.cipher.bits", bits)
    def cipherBits(bits: Int): Query = this.cipherBits(bits.toString)
    def cipherName(name: String) = addFilter("ssl.cipher.name", name)

    object certificate {
      def algorithm(algorithm: String) = addFilter("ssl.cert.alg", algorithm)
      def expired = addFilter("ssl.cert.expired", "true")
      def notExpired = addFilter("ssl.cert.expired", "false")
      def extension(extension: String) = addFilter("ssl.cert.extension", extension)
      def serialNumber(numberOrHex: String) = addFilter("ssl.cert.serial", numberOrHex)
      def serialNumber(number: BigInt): Query = this.serialNumber(number.toString)
      def publicKeyBits(bits: String) = addFilter("ssl.cert.pubkey.bits", bits)
      def publicKeyType(keyType: String) = addFilter("ssl.cert.pubkey.type", keyType)
    }

    object version {
      val SSLv2 = apply("SSLv2")
      val SSLv3 = apply("SSLv3")
      val TLSv1 = apply("TLSv1")
      val TLSv1_1 = apply("TLSv1.1")
      val TLSv1_2 = apply("TLSv1.2")
      def apply(versionName: String) = addFilter("ssl.version", versionName)
    }
  }

  def withHTMLContaining(search: String) = addFilter("http.html", search)
  def withHTMLHash(hash: String) = addFilter("http.html_hash", hash)

  object withHTTP {
    def component(componentName: String) = addFilter("http.component", componentName)
    def componentCategory(category: String) = addFilter("http.component_category", category)
    def title(pageTitle: String) = addFilter("http.title", pageTitle)

    object status {
      // List of HTTP status codes with their associated filter applies. Allows for stuff like .withHTTP.status.NOT_FOUND
      val CONTINUE = apply(100)
      val SWITCHING_PROTOCOLS = apply(101)
      val OK = apply(200)
      val CREATED = apply(201)
      val ACCEPTED = apply(202)
      val NON_AUTHORITATIVE_INFORMATION = apply(203)
      val NO_CONTENT = apply(204)
      val RESET_CONTENT = apply(205)
      val PARTIAL_CONTENT = apply(206)
      val MULTI_STATUS = apply(207)
      val MULTIPLE_CHOICES = apply(300)
      val MOVED_PERMANENTLY = apply(301)
      val FOUND = apply(302)
      val SEE_OTHER = apply(303)
      val NOT_MODIFIED = apply(304)
      val USE_PROXY = apply(305)
      val TEMPORARY_REDIRECT = apply(307)
      val PERMANENT_REDIRECT = apply(308)
      val BAD_REQUEST = apply(400)
      val UNAUTHORIZED = apply(401)
      val PAYMENT_REQUIRED = apply(402)
      val FORBIDDEN = apply(403)
      val NOT_FOUND = apply(404)
      val METHOD_NOT_ALLOWED = apply(405)
      val NOT_ACCEPTABLE = apply(406)
      val PROXY_AUTHENTICATION_REQUIRED = apply(407)
      val REQUEST_TIMEOUT = apply(408)
      val CONFLICT = apply(409)
      val GONE = apply(410)
      val LENGTH_REQUIRED = apply(411)
      val PRECONDITION_FAILED = apply(412)
      val REQUEST_ENTITY_TOO_LARGE = apply(413)
      val REQUEST_URI_TOO_LONG = apply(414)
      val UNSUPPORTED_MEDIA_TYPE = apply(415)
      val REQUESTED_RANGE_NOT_SATISFIABLE = apply(416)
      val EXPECTATION_FAILED = apply(417)
      val IM_A_TEAPOT = apply(418)
      val UNPROCESSABLE_ENTITY = apply(422)
      val LOCKED = apply(423)
      val FAILED_DEPENDENCY = apply(424)
      val UPGRADE_REQUIRED = apply(426)
      val TOO_MANY_REQUESTS = apply(429)
      val INTERNAL_SERVER_ERROR = apply(500)
      val NOT_IMPLEMENTED = apply(501)
      val BAD_GATEWAY = apply(502)
      val SERVICE_UNAVAILABLE = apply(503)
      val GATEWAY_TIMEOUT = apply(504)
      val HTTP_VERSION_NOT_SUPPORTED = apply(505)
      val INSUFFICIENT_STORAGE = apply(507)

      def apply(statusCode: Int) = addFilter("http.status", statusCode.toString)
    }
  }
}
