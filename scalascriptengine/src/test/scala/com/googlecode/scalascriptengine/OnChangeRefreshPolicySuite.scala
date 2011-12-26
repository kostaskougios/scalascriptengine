package com.googlecode.scalascriptengine
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import java.io.File
import scalascriptengine._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 25 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class OnChangeRefreshPolicySuite extends FunSuite with ShouldMatchers {

	val sourceDir = new File("testfiles/CompilationSuite")

	test("code modifications are reloaded immediatelly") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.onChangeRefresh(destDir)
		copyFromSource(new File(sourceDir, "v1/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 1
		sse.versionNumber should be === 1
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 2
		sse.versionNumber should be === 1
		copyFromSource(new File(sourceDir, "v2/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
		sse.numberOfFilesChecked should be === 3
		sse.versionNumber should be === 2
	}

	test("code modifications are reloaded according to recheckEveryMillis") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.onChangeRefresh(destDir, 2000)
		copyFromSource(new File(sourceDir, "v1/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 1
		sse.versionNumber should be === 1
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 1
		sse.versionNumber should be === 1
		copyFromSource(new File(sourceDir, "v2/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 1
		Thread.sleep(2100)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
		sse.versionNumber should be === 2
		sse.numberOfFilesChecked should be === 2
	}
}