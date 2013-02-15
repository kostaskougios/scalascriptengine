package com.googlecode.scalascriptengine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import java.security.AccessControlException
import org.scalatest.BeforeAndAfterAll

/**
 * @author		konstantinos.kougios
 *
 *                7 Oct 2012
 */
@RunWith(classOf[JUnitRunner])
class SandboxSuite extends FunSuite with ShouldMatchers with BeforeAndAfterAll {
	val sourceDir = new File("testfiles/SandboxSuite")
	val config = ScalaScriptEngine.defaultConfig(sourceDir).copy(
		classLoaderConfig = ClassLoaderConfig.default.copy(
			protectPackages = Set("javax.swing"),
			protectClasses = Set("java.lang.Thread") // note: still threads can be created via i.e. Executors
		)
	)
	System.setProperty("script.classes", config.targetDirs.head.toURI.toString)
	System.setProperty("java.security.policy", new File("testfiles/SandboxSuite/test.policy").toURI.toString)
	val sseSM = new SSESecurityManager(new SecurityManager)
	System.setSecurityManager(sseSM)

	val sse = ScalaScriptEngine.onChangeRefresh(config, 5)
	sse.deleteAllClassesInOutputDirectory
	sse.refresh

	test("will prevent access of a package") {
		intercept[AccessControlException] {
			sse.newInstance[TestClassTrait]("test.TryPackage").result
		}
	}

	test("will prevent creating a thread") {
		intercept[AccessControlException] {
			sse.newInstance[TestClassTrait]("test.TryThread").result
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

	test("sandbox eval") {
		intercept[AccessControlException] {
			val ect = EvalCode.with1Arg[String, String]("s", "s+classOf[java.lang.Thread].getName", config.classLoaderConfig)
			val f = ect.newInstance
			f("hi")
		}
	}

	override def afterAll =
		System.setSecurityManager(null)
}
