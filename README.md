# Prototype: Back-pressure with Akka Streams

### Requirements
* Scala 2.11
* sbt 0.13.8

### Running the sample
* `sbt "runMain net.sini.prototypes.akka.Streams"`
* terminate the process with `^C`

### Goal
Primarily to experiment with Akka's built-in support for back-pressure reactive streams.

### Expected results
In order to experiment with [Akka Streams](https://doc.akka.io/docs/akka/current/stream/stream-introduction.html), 
this sample application:

* sets up a [Source](https://doc.akka.io/docs/akka/current/stream/stream-flows-and-basics.html#core-concepts) from the sample input file included in this repository.
    * That sample file contains log entries, that our Streams-based system is going to process.
* also sets up a couple of Sinks to persist various pieces of the processed log entries to disk.
    * There isn't much processing at all in this prototype, given our goal to experiment with Streams' backpressure handling.
    * We could initiate more advanced, potentially costly processing, including computing log entry statistics by time period, indexing, etc.
* finally, defines (using the Streams DSL) and runs a simple Graph.
    * The graph includes a broadcaster and the two sinks above.
    * One of those sinks is artificially slowed down, to demonstrate how the source ends up slowing down its production of log file entries.   
