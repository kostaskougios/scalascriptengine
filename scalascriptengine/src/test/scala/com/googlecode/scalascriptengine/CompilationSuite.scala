package com.googlecode.scalascriptengine

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File

/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class CompilationSuite extends FunSuite with ShouldMatchers {

	val sourceDir = new File("testfiles/CompilationSuite")
	val classPath = new File("testfiles/lib").listFiles.filter(_.getName.endsWith(".jar")).toSet + new File("target/test-classes")

	test("scala files to compiled classes") {
		val sse = ScalaScriptEngine(sourceDir, classPath)
		sse.newInstance(sourceDir, "test.MyClass")
		new File(sse.outputDir, "test/MyClass.class").exists should be(true)
		new File(sse.outputDir, "test/Dep1.class").exists should be(true)
	}

	test("scala files correct") {
		val sse = ScalaScriptEngine(sourceDir, classPath)
		val tct: TestClassTrait = sse.newInstance(sourceDir, "test.MyClass")
		tct.result should be === "ok"
	}

	test("deleteAllClassesInOutputDirectory deletes all class files") {
		val sse = ScalaScriptEngine(sourceDir, classPath)
		sse.newInstance(sourceDir, "test.MyClass")
		sse.deleteAllClassesInOutputDirectory
		new File(sse.outputDir, "test/MyClass.class").exists should be(false)
		new File(sse.outputDir, "test/Dep1.class").exists should be(false)
	}
}