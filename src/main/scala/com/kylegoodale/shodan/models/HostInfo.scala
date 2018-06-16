package com.kylegoodale.shodan.models

import play.api.libs.json._

/**
  *
  * @param hostnames - An array of strings containing all of the hostnames that have been assigned to the IP address for this device.
  * @param ports -  The port number that the service is operating on.
  * @param asn - The autonomous system number
  * @param last_update - Timestamp of the last update in UTC time
  * @param city - City of the device
  * @param country_name - The name of the country the device is located in
  * @param country_code - The 2 digit country code of the device
  * @param country_code3 - The 3 digit country code of the device
  * @param area_code - The area code for the device's location. Only available for the US.
  * @param longitude - The longitude for the geolocation of the device.
  * @param latitude - The latitude for the geolocation of the device.
  * @param data - A List of Banners associated with this device
  * @param postal_code - The postal code for the device's location.
  * @param region_code - The name of the region where the device is located.
  * @param dma_code - The designated market area code for the area where the device is located. Only available for the US.
  * @param org - The name of the organization that is assigned the IP space for this device.
  * @param ip - The IP address of the host as an integer.
  * @param ipv6 - The IPv6 address of the host as a string. If this is present then the "ip" and "ip_str" fields wont be.
  * @param ip_str - The IP address of the host as a string.
  * @param vulns - An array of Vulnerabilities found on this device
  * @param tags - An array of tags this device has
  */
case class HostInfo(
  hostnames: List[String],
  ports: List[Int],
  isp: String,
  last_update: String,
  country_name: String,
  country_code: String,
  country_code3: String,
  longitude: Double,
  latitude: Double,
  data: List[Banner],

  /** Optional params **/
  asn: Option[String],
  city: Option[String],
  postal_code: Option[String],
  region_code: Option[String],
  dma_code: Option[Int],
  area_code: Option[Int],
  org: Option[String],
  ip: Option[Long],
  ipv6: Option[String],
  ip_str: Option[String],
  vulns: Option[List[String]],
  tags: Option[List[String]],
)

// Companion object containing our json reader
object HostInfo {
  implicit val hostInfoReads = Json.reads[HostInfo]
}

