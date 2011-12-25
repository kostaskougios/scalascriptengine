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
	// parent classloader will contain scala-lib and all test-compiled classes
	val classPath = Set[File]()

	test("will load a class") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/test"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		scl.refresh
		val tct: TestClassTrait = scl.newInstance("test.Test")
		tct.result should be === "v1"
	}

	test("will re-load a class") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/test"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		scl.refresh
		val tctV1: TestClassTrait = scl.newInstance("test.Test")
		tctV1.result should be === "v1"

		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2/test"), destDir)
		scl.refresh
		val tctV2: TestClassTrait = scl.newInstance("test.Test")
		tctV2.result should be === "v2"
	}

	test("reloading a class propagates to dependent classes") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/test"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		scl.refresh
		val tctV1: TestClassTrait = scl.newInstance("test.TestDep")
		tctV1.result should be === "TestDep:v1"

		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2/test"), destDir)
		scl.refresh
		val tctV2: TestClassTrait = scl.newInstance("test.TestDep")
		tctV2.result should be === "TestDep:v2"
	}

	test("using both v1 and v2 classes") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/test"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		scl.refresh

		val tctV1: TestClassTrait = scl.newInstance("test.Test")
		val tcpV1: TestParamTrait = scl.newInstance("test.TestParam")
		tcpV1.result(tctV1) should be === "TP:v1"

		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2/test"), destDir)
		scl.refresh

		val tcpV2: TestParamTrait = scl.newInstance("test.TestParam")
		tcpV2.result(tctV1) should be === "TP:v1"

		val tctV2: TestClassTrait = scl.newInstance("test.Test")
		tcpV2.result(tctV2) should be === "TP:v2"
	}

	test("will re-load a val contained in a class") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1/test"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		scl.refresh
		val tctV1: TestValTrait = scl.newInstance("test.TestVal")
		tctV1.x should be === 1

		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2/test"), destDir)
		scl.refresh
		val tctV2: TestValTrait = scl.newInstance("test.TestVal")
		tctV2.x should be === 2
	}
}