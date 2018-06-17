package com.kylegoodale.shodan.models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object SearchTokenResult {
  implicit val searchTokenResultReads: Reads[SearchTokenResult] = (
    (__ \ "attributes").read[JsValue].map {
      case obj: JsObject => obj.value.mapValues {
        case arr: JsArray => arr.value.map{
          case str: JsString => str.value
          case num: JsNumber => num.toString()
          case _ => ""
        }.toList
        case _ => List[String]()
      }.toMap
      case _ => Map[String, List[String]]()
    } and
    (__ \ "errors").read[List[String]] and
    (__ \ "string").read[String] and
    (__ \ "filters").read[List[String]]
  )(SearchTokenResult.apply _)
}

case class SearchTokenResult(
  attributes: Map[String, List[String]],
  errors: List[String],
  string: String,
  filters: List[String]
)
