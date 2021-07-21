import sbt.Keys._

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
      "io.lemonlabs" %% "scala-uri" % "1.5.1",
      "net.codingwell" %% "scala-guice" % "4.2.6",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.39.5" % "test",
      "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.39.5" % "test",

      "org.tpolecat" %% "doobie-core" % "0.12.1",
      "org.tpolecat" %% "doobie-hikari" % "0.12.1", // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % "0.12.1", // Postgres driver 42.2.19 + type mappings.
      "org.tpolecat" %% "doobie-specs2" % "0.12.1" % "test", // Specs2 support for typechecking statements.

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

