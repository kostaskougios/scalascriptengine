import sbt.Tests.{ Group, SubProcess }

name := "scalascriptengine"

organization := "com.googlecode.scalascriptengine"

version := "1.3.11"

pomIncludeRepository := { _ => false }

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/kostaskougios/scalascriptengine"))

scmInfo := Some(
	ScmInfo(
		url("https://github.com/kostaskougios/scalascriptengine"),
		"scm:https://github.com/kostaskougios/scalascriptengine.git"
	)
)

developers := List(
	Developer(
		id = "kostas.kougios@googlemail.com",
		name = "Konstantinos Kougios",
		email = "kostas.kougios@googlemail.com",
		url = url("https://github.com/kostaskougios")
	)
)

publishMavenStyle := true

publishTo := {
	val nexus = "https://oss.sonatype.org/"
	if (isSnapshot.value)
		Some("snapshots" at nexus + "content/repositories/snapshots")
	else
		Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

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
def forkedJvmPerTest(testDefs: Seq[TestDefinition]) = testDefs.groupBy(
	test => test.name match {
		case "com.googlecode.scalascriptengine.SandboxSuite" =>
			test.name
		case _ => "global"
	}
).map { case (name, tests) =>
	Group(
		name = name,
		tests = tests,
		runPolicy = SubProcess(ForkOptions())
	)
}.toSeq

//definedTests in Test returns all of the tests (that are by default under src/test/scala).
testGrouping in Test <<= (definedTests in Test) map forkedJvmPerTest

testOptions in Test += Tests.Argument("-oF")
