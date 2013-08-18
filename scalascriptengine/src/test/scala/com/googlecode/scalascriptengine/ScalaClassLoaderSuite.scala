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
 *         23 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class ScalaClassLoaderSuite extends FunSuite with ShouldMatchers
{
	val sourceDir = new File("testfiles/ScalaClassLoaderSuite")
	// parent classloader will contain scala-lib and all test-compiled classes
	val classPath = Set[File]()

	def classLoader(sourceDir: File, classPath: Set[File]) =
		new ScalaClassLoader(Set(sourceDir), classPath, Thread.currentThread.getContextClassLoader, ClassLoaderConfig.default)

	test("listeners are invoked") {
		val destDir = newTmpDir("defaultpackage")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "default"), destDir)
		var count = 0
		val scl = new ScalaClassLoader(
			Set(destDir),
			classPath,
			Thread.currentThread.getContextClassLoader,
			ClassLoaderConfig.default.copy(classLoadingListeners = ((className: String, clz: Class[_]) => {
				if (className == "Test" && classOf[TestClassTrait].isAssignableFrom(clz)) count += 1
			}) :: Nil))
		scl.newInstance[TestClassTrait]("Test")
		count should be(1)
	}

	test("load a class on the default package") {
		val destDir = newTmpDir("defaultpackage")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "default"), destDir)
		val scl = classLoader(destDir, classPath)
		val tct = scl.newInstance[TestClassTrait]("Test")
		tct.result should be === "v2"
	}

	test("will load a class") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1"), destDir)
		val scl = classLoader(destDir, classPath)
		val tct = scl.newInstance[TestClassTrait]("test.Test")
		tct.result should be === "v1"
	}

	test("loads dependent classes") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1"), destDir)
		val scl = classLoader(destDir, classPath)
		val tctV1 = scl.newInstance[TestClassTrait]("test.TestDep")
		tctV1.result should be === "TestDep:v1"
	}

	test("using both v1 and v2 classes") {
		val destDir = newTmpDir("dynamicclass")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1"), destDir)
		val scl1 = classLoader(destDir, classPath)

		val tctV1 = scl1.newInstance[TestClassTrait]("test.Test")
		val tcpV1 = scl1.newInstance[TestParamTrait]("test.TestParam")
		tcpV1.result(tctV1) should be === "TP:v1"

		cleanDestinationAndCopyFromSource(new File(sourceDir, "v2"), destDir)
		val scl2 = classLoader(destDir, classPath)

		val tcpV2 = scl2.newInstance[TestParamTrait]("test.TestParam")
		tcpV2.result(tctV1) should be === "TP:v1"

		val tctV2 = scl2.newInstance[TestClassTrait]("test.Test")
		tcpV2.result(tctV2) should be === "TP:v2"
	}
}