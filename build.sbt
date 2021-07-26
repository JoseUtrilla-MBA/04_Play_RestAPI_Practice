import sbt.Keys._

lazy val doobieVersion = "0.12.1"
lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin, Common)
  .settings(
    name := "ProductsRestAPI",
    version := "1.0",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      guice,
      "org.joda" % "joda-convert" % "2.2.1",
      "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
      "com.typesafe" % "config" % "1.4.1",
      "net.codingwell" %% "scala-guice" % "4.2.6",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,

      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion, // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % doobieVersion, // Postgres driver 42.2.19 + type mappings.
      "org.tpolecat" %% "doobie-specs2" % doobieVersion % "test", // Specs2 support for typechecking statements.

      "org.postgresql" % "postgresql" % "42.2.15",
      "org.scalactic" %% "scalactic" % "3.2.9",
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
      "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.39.5" % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.39.5" % Test

    ),
    Test / fork := true,

    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )

lazy val gatlingVersion = "3.3.1"
lazy val gatling = (project in file("gatling"))
  .enablePlugins(GatlingPlugin)
  .settings(
    scalaVersion := "2.12.13",
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % Test,
      "io.gatling" % "gatling-test-framework" % gatlingVersion % Test
    )
  )

