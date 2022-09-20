ThisBuild / organization := "dev.lucasgrey"
ThisBuild / version := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
ThisBuild / scalaVersion := "2.13.8"

// Workaround for scala-java8-compat issue affecting Lagom dev-mode
// https://github.com/lagom/lagom/issues/3344
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always

lagomCassandraEnabled in ThisBuild := false
lagomUnmanagedServices in ThisBuild := Map(
  "cas_native" -> "http://localhost:9042"
)

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

val AkkaVersion = "2.6.9"
val AkkaHttpVersion = "10.2.9"
val akkaDependencies = Seq(
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.1.4",
  "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % "1.6.7",
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "org.scalatest" %% "scalatest" % "3.2.12" % "test",
  "io.kamon" %% "kamon-jaeger" % "2.5.4",
  "io.kamon" %% "kamon-bundle" % "2.5.4",
  "io.kamon" %% "kamon-prometheus" % "2.5.4",
  "com.stripe" % "stripe-java" % "21.0.0",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42"
)

lazy val `payment-api` = (project in file("payment-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(
    `trakkie-commons`
  )

lazy val `payment-impl` = (project in file("payment-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= (Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslPersistenceJdbc,
      lagomScaladslPersistence,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      lagomScaladslAkkaDiscovery,
      macwire,
      scalaTest
    ) ++ akkaDependencies),
    dockerBaseImage := "ghcr.io/graalvm/graalvm-ce:latest",
    javaOptions in Universal += "-Dpidfile.path=/dev/null"
//    dockerEntrypoint ++= """ -Dconfig.file=/opt/docker/conf/config/application.conf""".split(" ").toSeq,
//    dockerCommands := dockerCommands.value.flatMap {
//      case ExecCmd("ENTRYPOINT", args@_*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
//      case v => Seq(v)
//    },
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(
    `payment-api`
  )

lazy val `trakkie-commons` = (project in file("trakkie-commons"))
  .settings(
    libraryDependencies ++= (Seq(
      lagomScaladslApi
    ) ++ akkaDependencies)
  )
