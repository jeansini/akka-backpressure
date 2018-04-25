package net.sini.prototypes.akka

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import net.sini.prototypes.util.Entry

import scala.concurrent._
import scala.concurrent.duration._


object Streams  {

  def inputFileSource(filename: String): Source[ByteString, Future[IOResult]] = {
    val file = Paths.get(filename)
    FileIO.fromPath(file)
  }

  def outputFileSink(filename: String): Sink[String, Future[IOResult]] = {
    Flow[String]
      .alsoTo(Sink.foreach(s => println(s"Appending to $filename: $s")))
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)
  }

  def main(args: Array[String]): Unit = {

    val config    = ConfigFactory.load("streams")

    implicit val system:        ActorSystem       = ActorSystem("StreamSystem", config)
    implicit val materializer:  ActorMaterializer = ActorMaterializer()

    val buffered    = config.getBoolean ("streams.slow.sink.buffer.enabled")
    val bufferSize  = config.getInt     ("streams.slow.sink.buffer.size")

    val sink1:      Sink[String, Future[IOResult]]  = outputFileSink(config.getString("streams.sink.1.output.file"))
    val s2:         Sink[String, Future[IOResult]]  = outputFileSink(config.getString("streams.sink.2.output.file"))

    val rawLogFile = inputFileSource(config.getString("streams.input.file"))
                      .via(Framing.delimiter(ByteString("\n"), Int.MaxValue, allowTruncation = true).map(_.utf8String))

    // We're producing a Source of Entry instances, and connecting it to various consumer Sinks via a Graph.

    // We're artificially slowing down sink2, to illustrate the resulting publisher behaviors in terms of back-pressure.
    // In a more realistic scenario the back-pressure would originate from expensive procesing in one of the consuming Sinks.
    val slowSink        = Flow[String].throttle(1, 500.millis, 1, ThrottleMode.shaping)

    // based on config option in streams.conf
    val sink2           = if(buffered) {
      // slow.sink.buffered = true: back-pressure signaling is delayed until the slow sink buffer is full.
      Flow[String].buffer(bufferSize, OverflowStrategy.backpressure).via(slowSink).toMat(s2)(Keep.right)
    } else {
      // slow.sink.buffered = false: back-pressure signaled to source immediately.
      Flow[String].via(slowSink).toMat(s2)(Keep.right)
    }

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit o =>

      import GraphDSL.Implicits._
      val broadcast = o.add(Broadcast[Entry](2))
      rawLogFile.map(Entry.fromRaw)         ~> broadcast.in
      broadcast.out(0).map(_.timedMessage)  ~> sink1
      broadcast.out(1).map(_.tags)          ~> sink2
      ClosedShape
    })

    g.run()
  }
}
