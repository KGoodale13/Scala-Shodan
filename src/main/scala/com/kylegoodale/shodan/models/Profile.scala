package com.kylegoodale.shodan.models

import play.api.libs.json.Json


object Profile {
  implicit val profileReads = Json.reads[Profile]
}
case class Profile(member: Boolean, credits: Int, display_name: Option[String], created: String)
