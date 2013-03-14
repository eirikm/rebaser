name := "rebaser"

organization := "rebaser"

version := "1.0-SNAPSHOT"

scalaVersion := "2.9.2"

mainClass in (Compile, run) := Some("gui.RebaserApp")
//mainClass in (Compile, run) := Some("rebaser.gui.Spike")

libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-swing" % "2.9.2",
    // test
    "org.scalatest" %% "scalatest" % "1.8" % "test",
    "junit" % "junit" % "4.10" % "test",
    // jgit
    "com.madgag" % "org.eclipse.jgit" % "2.2.0.0.2-UNOFFICIAL-ROBERTO-RELEASE",
    "com.madgag" % "org.eclipse.jgit.junit" % "2.2.0.0.2-UNOFFICIAL-ROBERTO-RELEASE"
    )

// one-jar config
seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
