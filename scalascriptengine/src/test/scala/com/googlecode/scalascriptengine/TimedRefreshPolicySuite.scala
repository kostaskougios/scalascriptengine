package com.googlecode.scalascriptengine

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scalascriptengine._
import java.io.File
import org.scala_tools.time.Imports._

/**
 * @author kostantinos.kougios
 *
 * 25 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class TimedRefreshPolicySuite extends FunSuite with ShouldMatchers {

	val sourceDir = new File("testfiles/versions")

	test("after compilation error, valid version is used") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.timedRefresh(destDir, () => DateTime.now + 500.millis)
		sse.deleteAllClassesInOutputDirectory
		try {
			copyFromSource(new File(sourceDir, "v1/reload"), destDir)
			sse.refresh
			sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
			copyFromSource(new File("testfiles/erroneous/ve/reload"), destDir)
			Thread.sleep(3000)
			sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
			copyFromSource(new File(sourceDir, "v2/reload"), destDir)
			Thread.sleep(3000)
			sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
		} finally {
			sse.shutdown
		}
	}
	test("code modifications are reloaded in time") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.timedRefresh(destDir, () => DateTime.now + 500.millis)
		sse.deleteAllClassesInOutputDirectory
		try {
			copyFromSource(new File(sourceDir, "v1/reload"), destDir)
			sse.refresh
			sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
			copyFromSource(new File(sourceDir, "v2/reload"), destDir)
			sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
			Thread.sleep(3000)
			sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
			copyFromSource(new File(sourceDir, "v1/reload"), destDir)
			Thread.sleep(3000)
			sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		} finally {
			sse.shutdown
		}
	}
}