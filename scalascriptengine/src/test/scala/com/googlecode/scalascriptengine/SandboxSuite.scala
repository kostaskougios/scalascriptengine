package com.googlecode.scalascriptengine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import scalascriptengine._
import java.security.AccessControlException

/**
 * @author		konstantinos.kougios
 *
 * 7 Oct 2012
 */
@RunWith(classOf[JUnitRunner])
class SandboxSuite extends FunSuite with ShouldMatchers {
	val sourceDir = new File("testfiles/SandboxSuite")
	System.setProperty("java.security.policy", new File("testfiles/SandboxSuite/test.policy").toURI.toString)
	val sseSM = new SSESecurityManager(Some(new SecurityManager))
	System.setSecurityManager(sseSM)

	test("will prevent access to a file") {
		val sse = ScalaScriptEngine.withoutRefreshPolicy(sourceDir)
		sse.deleteAllClassesInOutputDirectory
		sse.refresh
		val tct = sse.newInstance[TestClassTrait]("test.TryFile")
		try {
			sseSM.secured {
				tct.result should be === "directory"
				fail()
			}
		} catch {
			case e: AccessControlException => // ok
		}
	}

}
