package com.googlecode.scalascriptengine
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File

/**
 * @author kostantinos.kougios
 *
 * 29 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class ResourcesSuite extends FunSuite with ShouldMatchers {
	val sourceDir = new File("testfiles/ResourcesSuite")

	test("loads resources from classpath") {
		val sse = ScalaScriptEngine.withoutRefreshPolicy(new File(sourceDir, "v1"))
		sse.refresh

		val t: TestClassTrait = sse.newInstance[TestClassTrait]("reload.Main")
		t.result should be === "v1"

	}
}