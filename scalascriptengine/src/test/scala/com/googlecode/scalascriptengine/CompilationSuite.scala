package com.googlecode.scalascriptengine

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import scalascriptengine._
/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class CompilationSuite extends FunSuite with ShouldMatchers {

	val sourceDir = new File("testfiles/CompilationSuite")
	val versionsDir = new File("testfiles/versions")
	//	val classPath = new File("testfiles/lib").listFiles.filter(_.getName.endsWith(".jar")).toSet + new File("target/test-classes")

	test("code modifications are reloaded") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.withoutRefreshPolicy(destDir)
		sse.deleteAllClassesInOutputDirectory
		for (i <- 1 to 5) {
			copyFromSource(new File(versionsDir, "v1/reload"), destDir)
			sse.refresh
			val v1: TestClassTrait = sse.newInstance("reload.Reload")
			v1.result should be === "v1"
			copyFromSource(new File(versionsDir, "v2/reload"), destDir)
			sse.refresh
			val v2: TestClassTrait = sse.newInstance("reload.Reload")
			v2.result should be === "v2"
		}
	}

	test("scala files to compiled classes") {
		val sse = ScalaScriptEngine.withoutRefreshPolicy(sourceDir)
		sse.deleteAllClassesInOutputDirectory
		sse.refresh
		sse.newInstance("test.MyClass")
		new File(sse.outputDir, "test/MyClass.class").exists should be(true)
		new File(sse.outputDir, "test/Dep1.class").exists should be(true)
	}

	test("scala files correct") {
		val sse = ScalaScriptEngine.withoutRefreshPolicy(sourceDir)
		sse.deleteAllClassesInOutputDirectory
		sse.refresh
		val tct: TestClassTrait = sse.newInstance("test.MyClass")
		tct.result should be === "ok"
	}

	test("deleteAllClassesInOutputDirectory deletes all class files") {
		val sse = ScalaScriptEngine.withoutRefreshPolicy(sourceDir)
		sse.deleteAllClassesInOutputDirectory
		sse.refresh
		sse.newInstance("test.MyClass")
		sse.deleteAllClassesInOutputDirectory
		new File(sse.outputDir, "test/MyClass.class").exists should be(false)
		new File(sse.outputDir, "test/Dep1.class").exists should be(false)
	}
}