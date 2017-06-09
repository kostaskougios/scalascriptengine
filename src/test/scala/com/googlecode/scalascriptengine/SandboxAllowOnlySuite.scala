package com.googlecode.scalascriptengine

import java.io.File
import java.security.AccessControlException

import com.googlecode.scalascriptengine.classloading.ClassLoaderConfig
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
 * @author kostantinos.kougios
 *
 *         12 Oct 2012
 */
class SandboxAllowOnlySuite extends FunSuite
{
	val sourceDir = new File("testfiles/SandboxAllowOnlySuite")
	val allowedPackages = Set(
		"java.lang",
		"scala",
		"com.googlecode.scalascriptengine")
	val config = ScalaScriptEngine.defaultConfig(sourceDir).copy(
		classLoaderConfig = ClassLoaderConfig.Default.copy(
			allowed = {
				(pckg, name) =>
					allowedPackages(pckg) || pckg == "test"
			}
		)
	)
	val sse = ScalaScriptEngine.onChangeRefresh(config, 5)
	sse.deleteAllClassesInOutputDirectory()
	sse.refresh

	test("allow only specific packages, positive") {
		val ex = intercept[AccessControlException] {
			val t = sse.newInstance[TestClassTrait]("test.TryPackage")
			t.result
		}
		ex.getMessage should be("access to class javax.swing.Icon not allowed")
	}

	test("allow only specific packages, negative") {
		val t = sse.newInstance[TestClassTrait]("test.TryPackageAllow")
		t.result should be("allowed")
	}
}
