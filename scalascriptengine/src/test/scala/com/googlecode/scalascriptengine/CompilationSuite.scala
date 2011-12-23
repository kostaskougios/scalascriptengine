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
	val classPath = new File("testfiles/lib").listFiles.filter(_.getName.endsWith(".jar")).toSet + new File("target/test-classes")

	test("code modifications are reloaded") {
		val destDir = newTmpDir("dynamicsrc")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/reload"), destDir)
		val sse = ScalaScriptEngine(destDir, classPath)
		val v1: TestClassTrait = sse.newInstance(destDir, "reload.Reload")
		v1.result should be === "v1"
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2/reload"), destDir)
		val v2: TestClassTrait = sse.newInstance(destDir, "reload.Reload")
		v2.result should be === "v2"
	}
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