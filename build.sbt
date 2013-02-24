name:= "rebaser"

organization := "rebaser"

scalaVersion := "2.9.2"

mainClass in (Compile, run) := Some("gui.RebaserApp")

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.9.2"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "com.madgag" % "org.eclipse.jgit" % "2.2.0.0.2-UNOFFICIAL-ROBERTO-RELEASE"

libraryDependencies += "com.madgag" % "org.eclipse.jgit.junit" % "2.2.0.0.2-UNOFFICIAL-ROBERTO-RELEASE"

