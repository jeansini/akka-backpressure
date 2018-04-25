package net.sini.prototypes.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// very brittle log parser -- goal is to experiment with Streams.
// [INFO] [04/25/2018 13:14:38.834] [ClusterSystem-akka.actor.default-dispatcher-10] [akka://ClusterSystem@127.0.0.1:2551/user/factorialBackend] ...
case class Entry(level: String, timestamp: LocalDateTime, threadName: String, actorPath: String, message: String) {
  def timedMessage = s"ts=$timestamp, message=$message"
  def tags = s"ts=$timestamp, level=$level, thread=$threadName, actor=$actorPath"
}

object Entry {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS")

  def fromRaw(raw: String): Entry = {
    val tokens = raw.split("\\]", 5).map(_.replaceFirst("\\[","").trim)
    Entry(tokens(0), LocalDateTime.parse(tokens(1), formatter), tokens(2), tokens(3), tokens(4).replaceAll("\\] \\["," ").trim)
  }
}
