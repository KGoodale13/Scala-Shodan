package com.kylegoodale.shodan.models

import play.api.libs.json._
import ai.x.play.json.Jsonx

/**
  * Case class for shodan's banner specifications (https://developer.shodan.io/api/banner-specification)
  *
  * @param asn - The autonomous system number
  * @param data - Contains the banner information for the service
  * @param ip - Contains the banner information for the service
  * @param ip_str - The IP address of the host as a string
  * @param ipv6 - The IPv6 address of the host as a string. If this is present then the "ip" and "ip_str" fields wont be
  * @param port - The port number that the service is operating on
  * @param timestamp - The timestamp for when the banner was fetched from the device in the UTC timezone.
  * @param hostnames - An array of strings containing all of the hostnames that have been assigned to the IP address for this device.
  * @param vulns - A list of vulnerabilities found on this server
  * @param domains -  An array of strings containing the top-level domains for the hostnames of the device. This is a utility property in case you want to filter by TLD instead of subdomain. It is smart enough to handle global TLDs with several dots in the domain
  * @param location - An object containing all of the location information for the device (See Location case class)
  * @param org - The name of the organization that is assigned the IP space for this device.
  * @param isp - The ISP that is providing the organization with the IP space for this device. Consider this the "parent" of the organization in terms of IP ownership.
  * @param os - The operating system that powers the device.
  * @param transport - Either "udp" or "tcp" to indicate which IP transport protocol was used to fetch the information
  * @param uptime - The number of minutes that the device has been online.
  * @param link - The network link type. Possible values are: "Ethernet or modem", "generic tunnel or VPN", "DSL", "IPIP or SIT", "SLIP", "IPSec or GRE", "VLAN", "jumbo Ethernet", "Google", "GIF", "PPTP", "loopback", "AX.25 radio modem".
  * @param title - The title of the website as extracted from the HTML source.
  * @param html - The raw HTML source for the website.
  * @param product - The name of the product that generated the banner.
  * @param version - The version of the product that generated the banner.
  * @param devicetype - The type of device (webcam, router, etc.).
  * @param info - Miscellaneous information that was extracted about the product.
  * @param cpe - The relevant Common Platform Enumeration for the product or known vulnerabilities if available
  * @param hash - A hash of this Banners data
  * @param tags - Tags associated with this device
  */
case class Banner(
  data: String,
  port: Int,
  timestamp: String,
  domains: List[String],
  location: Location,
  transport: String,
  isp: Option[String],
  org: Option[String],
  hostnames: Option[List[String]],
  asn: Option[String],
  os: Option[String],
  vulns: Option[Map[String, Vulnerability]], // No specification is listed for this so we are unable to parse this into any standard types
  ip: Option[Long],
  ip_str: Option[String],
  ipv6: Option[String],
  uptime: Option[Long],
  link: Option[String],
  title: Option[String],
  html: Option[String],
  product: Option[String],
  version: Option[String],
  devicetype: Option[String],
  info: Option[String],
  cpe: Option[List[String]],
  ssl: Option[SSLInfo],
  hash: Option[Long],
  tags: Option[List[String]],
  http: Option[HttpData]
)



// Companion object containing json readers for our banner specification and associated classes
object Banner {
  import ai.x.play.json.implicits.optionWithNull

  implicit val vulnerabilityReads = Json.format[Vulnerability]
  implicit val locationReads = Json.format[Location]
  implicit val dhparamsReads = Json.format[DiffieHellmanParams]
  implicit val cipherReads = Json.format[Cipher]
  implicit val sslInfoReads = Json.format[SSLInfo]
  implicit val htmlComponentsData = Json.format[HTMLComponentsData]
  implicit val redirectDataReads = Json.format[RedirectData]
  implicit val httpdataReads = Json.format[HttpData]

  // Combine our two separate reads to create a single reader for our Banner case class that fits all the params
  implicit val bannerReads = Jsonx.formatCaseClass[Banner]
}

/**
  * Case class for Shodan's Banner Location specifications
  *
  * @param area_code - The area code for the device's location. Only available for the US.
  * @param city - The name of the city where the device is located.
  * @param country_code - The 2-letter country code for the device location.
  * @param country_code3 - The 3-letter country code for the device location
  * @param country_name - The name of the country where the device is located.
  * @param dma_code - The designated market area code for the area where the device is located. Only available for the US.
  * @param latitude - The latitude for the geolocation of the device.
  * @param longitude - The longitude for the geolocation of the device.
  * @param postal_code - The postal code for the device's location.
  * @param region_code - The name of the region where the device is located.
  */
case class Location(
  area_code: Option[Int],
  city: Option[String],
  country_code: String,
  country_code3: String,
  country_name: Option[String],
  dma_code: Option[Int],
  latitude: Double,
  longitude: Double,
  postal_code: Option[String],
  region_code: Option[String]
)

/**
  * Case class for Shodan's SSL Info Banner specifications
  * @param chain - An array of certificates, where each string is a PEM-encoded SSL certificate. This includes the user SSL certificate up to its root certificate.
  * @param versions -  A list of SSL versions that are supported by the server. If a version isnt supported the value is prefixed with a "-".
  * @param dhparams - The Diffie-Hellman parameters if available: "prime", "public_key", "bits", "generator" and an optional "fingerprint" if we know which program generated these parameters.
  * @param cipher -  Preferred cipher for the SSL connection
  */
case class SSLInfo(
  chain: List[String],
  versions: List[String],
  dhparams: Option[DiffieHellmanParams],
  cipher: Option[Cipher]
)

case class DiffieHellmanParams(
  bits: Int,
  prime: Option[String],
  publicKey: Option[String],
  fingerprint: Option[String]
)

case class Cipher(
  bits: Int,
  version: String,
  name: String
)

case class Vulnerability(
  verified: Boolean,
  references: List[String],
  summary: Option[String]
)

/**
  * Data relating to the HTTP server running on this device
  * @param robots - The Robots.txt content
  * @param robots_hash - A hash of the robots.txt
  * @param securitytxt - The security.txt content
  * @param securitytxt_hash - Hash of the security.txt content
  * @param components - Components running on this web server listed as strings
  * @param location - The URL location the crawler was at when fetching this data
  * @param redirects - List of redirects that brought the crawler to this page
  * @param sitemap - List of pages on this site
  * @param sitemap_hash - Hash of the sitemap
  * @param host - The hostname of this webserver
  * @param server - The http server software running on this device i.e Apache 2.5
  * @param title - The title of the homepage
  * @param html - The html content of the homepage
  * @param html_hash - A hash of the html content
  */
case class HttpData(
  robots: Option[String],
  robots_hash: Option[Long],
  securitytxt: Option[String],
  securitytxt_hash: Option[Long],
  components: Option[Map[String, HTMLComponentsData]],
  location: Option[String],
  redirects: Option[List[RedirectData]],
  sitemap: Option[String],
  sitemap_hash: Option[String],
  host: Option[String],
  server: Option[String],
  title: Option[String],
  html: Option[String],
  html_hash: Option[Long]
)

case class HTMLComponentsData(categories: List[String])

case class RedirectData(host: String, data: String, location: String)



