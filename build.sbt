ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "akka-flow-indexer"
  )

resolvers +=
  "jitpack.io" at "https://jitpack.io"

val AkkaVersion = "2.7.0"
val AkkaHttpVersion = "10.4.0"
lazy val akkaManagementVersion =  "1.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.1.0",
  "com.lightbend.akka" %% "akka-projection-slick" % "1.3.1",
  "com.lightbend.akka" %% "akka-projection-eventsourced" % "1.3.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
  "org.postgresql" % "postgresql" % "42.5.4",
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.0",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.5",
  "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "com.typesafe.akka" %% "akka-persistence" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
  "io.kamon" %% "kamon-zipkin" % "2.6.0",
  "io.kamon" %% "kamon-bundle" % "2.6.0",
  "io.kamon" %% "kamon-prometheus" % "2.6.0",
  "io.circe" %% "circe-core" % "0.14.4",
  "io.circe" %% "circe-generic" % "0.14.4",
  "io.circe" %% "circe-parser" % "0.14.4",
  "com.softwaremill.macwire" %% "macros" % "2.5.8" % "provided",
  "com.softwaremill.macwire" %% "macrosakka" % "2.5.8" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.5.8",
  "com.softwaremill.macwire" %% "proxy" % "2.5.8",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",

  //3rd Party Repo
  "org.onflow" % "flow" % "0.21",
  "com.swissborg" %% "lithium" % "0.11.2",
  "com.github.tminglei" %% "slick-pg" % "0.21.1",
  "com.github.tminglei" %% "slick-pg_circe-json" % "0.21.1",
  "joda-time" % "joda-time" % "2.12.1"
)
