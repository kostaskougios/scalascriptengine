name := "scalascriptengine"

organization := "com.googlecode"

version := "1.3.11"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
	"commons-io" % "commons-io" % "2.1" % Test,
	"org.slf4j" % "slf4j-api" % "1.6.4",
	"ch.qos.logback" % "logback-classic" % "1.0.0",
	"org.scala-lang" % "scala-reflect" % "2.12.2",
	"org.scalatest" %% "scalatest" % "3.0.3",
	"org.scala-lang" % "scala-compiler" % "2.12.2",
	"joda-time" % "joda-time" % "2.9.9"
)

// fork in test cause there are conflicts with sbt classpath
fork in Test := true

testOptions in Test += Tests.Argument("-oF")

parallelExecution in Test := false