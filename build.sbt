name := "ScalaShodan"

version := "0.1"

scalaVersion := "2.12.6"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.0-M2"
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-json" % "2.0.0-M2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.7"
libraryDependencies += "ai.x" %% "play-json-extensions" % "0.10.0" // Extension to allow more than 22 parameters in our json case class mappings