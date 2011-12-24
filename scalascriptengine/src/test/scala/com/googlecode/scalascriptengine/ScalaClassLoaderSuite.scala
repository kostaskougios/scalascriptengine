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
 * 23 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class ScalaClassLoaderSuite extends FunSuite with ShouldMatchers {
	val sourceDir = new File("testfiles/ScalaClassLoaderSuite")
	val classPath = new File("testfiles/lib").listFiles.filter(_.getName.endsWith(".jar")).toSet + new File("target/test-classes")

	test("will load a class") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/test"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		scl.loadAll
		val tct: TestClassTrait = scl.newInstance("test.Test")
		tct.result should be === "v1"
	}

	test("will re-load a class") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/test"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		scl.loadAll
		val tctV1: TestClassTrait = scl.newInstance("test.Test")
		tctV1.result should be === "v1"

		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2/test"), destDir)
		scl.loadAll
		val tctV2: TestClassTrait = scl.newInstance("test.Test")
		tctV2.result should be === "v2"
	}

	test("reloading a class propagates to dependent classes") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/test"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		scl.loadAll
		val tctV1: TestClassTrait = scl.newInstance("test.TestDep")
		val cl = tctV1.getClass().getClassLoader()
		tctV1.result should be === "TestDep:v1"

		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2/test"), destDir)
		scl.loadAll
		val tctV2: TestClassTrait = scl.newInstance("test.TestDep")
		tctV2.result should be === "TestDep:v2"
	}
}