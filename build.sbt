import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "fd",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "1.0.4",
      "co.fs2" %% "fs2-io" % "1.0.4",
      "com.softwaremill.sttp" %% "core" % "1.6.0",
      "com.softwaremill.sttp" %% "async-http-client-backend-fs2" % "1.6.0",
      "org.typelevel" %% "cats-effect" % "1.3.1",
      "org.typelevel" %% "cats-core" % "2.0.0-M4",
      "org.http4s" %% "http4s-dsl" % "0.20.3",
      "org.http4s" %% "http4s-blaze-server" % "0.20.3"
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
