ThisBuild / scalaVersion := "3.3.4"
ThisBuild / organization := "com.proover"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "proover-verifier",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    libraryDependencies ++= Seq(
      // Leo-III's TPTP parser (https://github.com/leoprover/scala-tptp-parser).
      // Published only for Scala 2.13, consumed here from Scala 3.
      ("io.github.leoprover" %% "scala-tptp-parser" % "1.7.3")
        .cross(CrossVersion.for3Use2_13),
      // Princess theorem prover (https://github.com/uuverifiers/princess), used
      // as the external solver for free (plain) inference steps. Scala 2.13 only.
      ("io.github.uuverifiers" %% "princess" % "2025-06-25")
        .cross(CrossVersion.for3Use2_13),
      "org.scalameta" %% "munit" % "1.0.4" % Test
    )
  )
