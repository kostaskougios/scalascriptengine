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

	val safeClasspath = new File(".").toURI.toString
	System.setProperty("safe.classpath", safeClasspath)
	val sseSM = new SSESecurityManager(Some(new SecurityManager))
	System.setSecurityManager(sseSM)

	test("will prevent access to a file, positive") {
		val sse = ScalaScriptEngine.onChangeRefresh(sourceDir)
		sse.deleteAllClassesInOutputDirectory
		val constructors = sse.constructors[TestClassTrait]("test.TryFile")

		val ex = intercept[AccessControlException] {
			sseSM.secured {
				val tct = constructors.newInstance
				tct.result should be === "directory"
			}
		}
		ex.getPermission match {
			case fp: java.io.FilePermission if (fp.getActions == "read") =>
			// ok
			case _ => throw ex
		}
	}
}
