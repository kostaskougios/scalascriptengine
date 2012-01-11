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
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		val tct: TestClassTrait = scl.newInstance("test.Test")
		tct.result should be === "v1"
	}

	test("loads dependent classes") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1"), destDir)
		val scl = new ScalaClassLoader(destDir, classPath)
		val tctV1: TestClassTrait = scl.newInstance("test.TestDep")
		tctV1.result should be === "TestDep:v1"
	}

	test("using both v1 and v2 classes") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1"), destDir)
		val scl1 = new ScalaClassLoader(destDir, classPath)

		val tctV1: TestClassTrait = scl1.newInstance("test.Test")
		val tcpV1: TestParamTrait = scl1.newInstance("test.TestParam")
		tcpV1.result(tctV1) should be === "TP:v1"

		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2"), destDir)
		val scl2 = new ScalaClassLoader(destDir, classPath)

		val tcpV2: TestParamTrait = scl2.newInstance("test.TestParam")
		tcpV2.result(tctV1) should be === "TP:v1"

		val tctV2: TestClassTrait = scl2.newInstance("test.Test")
		tcpV2.result(tctV2) should be === "TP:v2"
	}
}