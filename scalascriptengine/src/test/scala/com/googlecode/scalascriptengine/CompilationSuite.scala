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
 *         22 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class CompilationSuite extends FunSuite with ShouldMatchers {

	val sourceDir = new File("testfiles/CompilationSuite")
	val versionsDir = new File("testfiles/versions")

	test("multiple source and target dirs") {
		val sourceDir1 = new File("testfiles/CompilationSuite1")
		val sourceDir2 = new File("testfiles/CompilationSuite2")
		val target1 = tmpOutputFolder(1)
		val target2 = tmpOutputFolder(2)
		val sse = ScalaScriptEngine.withoutRefreshPolicy(List(SourcePath(sourceDir1, target1), SourcePath(sourceDir2, target2)))
		sse.deleteAllClassesInOutputDirectory

		sse.refresh

		new File(target1, "A.class").exists should be(true)
		new File(target2, "B.class").exists should be(true)
	}

	test("code modifications are reloaded") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.withoutRefreshPolicy(SourcePath(destDir))
		sse.deleteAllClassesInOutputDirectory
		for (i <- 1 to 5) {
			copyFromSource(new File(versionsDir, "v1"), destDir)
			sse.refresh
			val v1 = sse.newInstance[TestClassTrait]("reload.Reload")
			v1.result should be === "v1"
			copyFromSource(new File(versionsDir, "v2"), destDir)
			sse.refresh
			val v2 = sse.newInstance[TestClassTrait]("reload.Reload")
			v2.result should be === "v2"
		}
	}

	test("scala files to compiled classes") {
		val sse = ScalaScriptEngine.withoutRefreshPolicy(SourcePath(sourceDir))
		sse.deleteAllClassesInOutputDirectory
		sse.refresh
		sse.newInstance[Any]("test.MyClass")
		new File(sse.config.targetDirs.head, "test/MyClass.class").exists should be(true)
		new File(sse.config.targetDirs.head, "test/Dep1.class").exists should be(true)
	}

	test("scala files correct") {
		val sse = ScalaScriptEngine.withoutRefreshPolicy(SourcePath(sourceDir))
		sse.deleteAllClassesInOutputDirectory
		sse.refresh
		val tct = sse.newInstance[TestClassTrait]("test.MyClass")
		tct.result should be === "ok"
	}

	test("deleteAllClassesInOutputDirectory deletes all class files") {
		val sse = ScalaScriptEngine.withoutRefreshPolicy(SourcePath(sourceDir))
		sse.deleteAllClassesInOutputDirectory
		sse.refresh
		sse.newInstance[Any]("test.MyClass")
		sse.deleteAllClassesInOutputDirectory
		new File(sse.config.targetDirs.head, "test/MyClass.class").exists should be(false)
		new File(sse.config.targetDirs.head, "test/Dep1.class").exists should be(false)
	}

	private def tmpOutputFolder(i: Int) = {
		val dir = new File(System.getProperty("java.io.tmpdir"), "scala-script-engine-classes" + i)
		dir.mkdir
		dir
	}

}