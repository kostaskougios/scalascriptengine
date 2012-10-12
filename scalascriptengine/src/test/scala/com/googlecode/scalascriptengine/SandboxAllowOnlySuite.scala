package com.googlecode.scalascriptengine

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import java.io.File

/**
 * @author kostantinos.kougios
 *
 * 12 Oct 2012
 */
@RunWith(classOf[JUnitRunner])
class SandboxAllowOnlySuite extends FunSuite with ShouldMatchers {
	val sourceDir = new File("testfiles/SandboxAllowOnlySuite")
	val config = ScalaScriptEngine.defaultConfig(sourceDir).copy(
		classLoaderConfig = ClassLoaderConfig.default.copy(
			allowedPackages = Set("java.lang", "scala.lang")
		)
	)

	test("allow only specific classes, positive") {
	}
}
