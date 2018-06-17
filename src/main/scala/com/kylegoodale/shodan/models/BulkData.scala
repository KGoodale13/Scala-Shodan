package com.kylegoodale.shodan.models

import play.api.libs.json.Json

object Dataset {
  implicit val datasetReads = Json.reads[Dataset]
}
case class Dataset(name: String, scope: String, description: String)

object DatasetFile {
  implicit val datasetFileReads = Json.reads[DatasetFile]
}
case class DatasetFile(url: String, timestamp: Long, name: String, size: Long)
