name := "Streams"

version := "1.0"

scalaVersion  := "2.12.5"

val akkaVersion   = "2.5.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
)
    