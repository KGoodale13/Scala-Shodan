package com.kylegoodale.shodan.models

import play.api.libs.json.Json

object APIInfo {
  implicit val apiInfoReads = Json.reads[APIInfo]
}

case class APIInfo(query_credits: Int, scan_credits: Int, telnet: Boolean, plan: String, https: Boolean, unlocked: Boolean)