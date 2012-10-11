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
	val config = ScalaScriptEngine.defaultConfig(sourceDir)
	System.setProperty("script.classes", config.outputDir.toURI.toString)
	System.setProperty("java.security.policy", new File("testfiles/SandboxSuite/test.policy").toURI.toString)
	val sseSM = new SSESecurityManager(new SecurityManager)
	System.setSecurityManager(sseSM)

	val sse = ScalaScriptEngine.onChangeRefresh(config, 5)
	sse.deleteAllClassesInOutputDirectory
	sse.refresh

	test("will prevent creating a thread") {
		intercept[AccessControlException] {
			sseSM.secured {
				sse.newInstance[TestClassTrait]("test.TryThread").result
			}
		}
	}

	test("will prevent access to a file") {
		val ex = intercept[AccessControlException] {
			sseSM.secured {
				val tct = sse.newInstance[TestClassTrait]("test.TryFile")
				tct.result should be === "directory"
			}
		}
		ex.getPermission match {
			case fp: java.io.FilePermission if (fp.getActions == "read" && fp.getName == "/tmp") =>
			// ok
			case _ => throw ex
		}
	}

	test("will allow access to a file") {
		sseSM.secured {
			val tct = sse.newInstance[TestClassTrait]("test.TryHome")
			tct.result should be === "directory"
		}
	}
}
