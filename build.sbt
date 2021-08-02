import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin, Common)
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    name := "ProductsRestAPI",
    version := "1.0",
    scalaVersion := "2.13.6",
    Test / fork := true,
    libraryDependencies ++= Seq(
      guice,
      evolutions,
      jdbc,

      //Config-Tools_____________________
      "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
      "com.typesafe" % "config" % "1.4.1",
      "net.codingwell" %% "scala-guice" % "4.2.6",

      //Persistence_______________
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion, // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % doobieVersion, // Postgres driver 42.2.19 + type mappings.
      "org.tpolecat" %% "doobie-specs2" % doobieVersion % "it", // Specs2 support for typechecking statements.
      "org.postgresql" % "postgresql" % "42.2.15",

      //Testing___________________
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "it, test",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "it, test",
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainerVersion % "it, test",
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testContainerVersion % "it, test"
    ),

    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )

lazy val doobieVersion = "0.12.1"
lazy val scalaTestVersion = "3.1.0"
lazy val testContainerVersion = "0.39.5"

