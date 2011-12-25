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
		val sse = ScalaScriptEngine(destDir)
		copyFromSource(new File(sourceDir, "v1/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		copyFromSource(new File(sourceDir, "v2/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
	}
}