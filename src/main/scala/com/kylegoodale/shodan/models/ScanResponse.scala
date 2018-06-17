package com.kylegoodale.shodan.models

import play.api.libs.json.Json

object ScanResponse {
  implicit val scanResponseReads = Json.reads[ScanResponse]
}
case class ScanResponse(id: String, count: Int, credits_left: Int)

object ScanStatus {
  implicit val scanStatusReads = Json.reads[ScanStatus]
}
case class ScanStatus(id: String, count: Int, status: String)