package com.googlecode.scalascriptengine

import java.io.File

import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * @author kostantinos.kougios
  *
  *         2 Jan 2012
  */
class EnhancersSuite extends FunSuite
{
	val sourceDir = new File("testfiles/FromClasspathFirst")

	test("from classpath first loads the already compiled version") {
		val sse = new ScalaScriptEngine(new Config(sourceDir)) with FromClasspathFirst
		sse.deleteAllClassesInOutputDirectory
		val t = sse.newInstance[TestClassTrait]("test.FromClasspathFirst")
		t.result should be("fcf:5")
	}

	test("from classpath first loads the script version") {
		val sse = new ScalaScriptEngine(new Config(sourceDir)) with RefreshSynchronously with FromClasspathFirst
		{
			val recheckEveryMillis: Long = 0
		}
		sse.deleteAllClassesInOutputDirectory
		val t = sse.newInstance[TestClassTrait]("test.FCF")
		t.result should be("not from classpath")
	}

	test("constructors use classpath class") {
		val sse = new ScalaScriptEngine(new Config(sourceDir)) with FromClasspathFirst
		sse.deleteAllClassesInOutputDirectory
		val constructors = sse.constructors[TestClassTrait]("test.FromClasspathFirst")
		val t = constructors.newInstance(8)
		t.result should be("fcf:8")
	}
}