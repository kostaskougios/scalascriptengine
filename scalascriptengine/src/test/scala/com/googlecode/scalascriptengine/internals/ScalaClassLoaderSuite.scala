package com.googlecode.scalascriptengine.internals

import java.io.File

import com.googlecode.scalascriptengine.classloading.ScalaClassLoader
import com.googlecode.scalascriptengine.scalascriptengine._
import com.googlecode.scalascriptengine.{ClassLoaderConfig, TestClassTrait, TestParamTrait}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}

/**
 * @author kostantinos.kougios
 *
 *         23 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class ScalaClassLoaderSuite extends FunSuite with Matchers
{
	val sourceDir = new File("testfiles/ScalaClassLoaderSuite")
	// parent classloader will contain scala-lib and all test-compiled classes
	val classPath = Set[File]()

	def classLoader(sourceDir: File, classPath: Set[File], config: ClassLoaderConfig = ClassLoaderConfig.Default) =
		new ScalaClassLoader(Set(sourceDir), classPath, Thread.currentThread.getContextClassLoader, config)

	test("class registry") {
		val cl = classLoader(new File(sourceDir, "v1"), Set(), config = ClassLoaderConfig.Default.copy(enableClassRegistry = true))
		cl.all.map(_.getName).toSet should be(Set("test.TestDep", "test.TestParam", "test.Test"))
	}

	test("classes of type") {
		val cl = classLoader(new File(sourceDir, "v1"), Set(), config = ClassLoaderConfig.Default.copy(enableClassRegistry = true))
		cl.withTypeOf[TestParamTrait] should be(List(cl.get("test.TestParam")))
	}

	test("listeners are invoked") {
		val destDir = newTmpDir("defaultpackage")
		cleanDestinationAndCopyFromSource(new File(sourceDir, "default"), destDir)
		var count = 0
		val scl = new ScalaClassLoader(
			Set(destDir),
			classPath,
			Thread.currentThread.getContextClassLoader,
			ClassLoaderConfig.Default.copy(classLoadingListeners = ((className: String, clz: Class[_]) => {
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
		tct.result should be === "result"
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

	//	test("stress test loads the same class") {
	//		val destDir = newTmpDir("dynamicclass")
	//		cleanDestinationAndCopyFromSource(new File(sourceDir, "v1"), destDir)
	//		val scl = classLoader(destDir, classPath)
	//
	//		for (j <- 0 to 1000) {
	//			println("go")
	//			val start=System.currentTimeMillis
	//			for (i <- 0 to 100000) {
	//				scl.newInstance[TestClassTrait]("test.Test")
	//			}
	//			val dt=System.currentTimeMillis-start
	//			println("dt="+dt)
	//			Thread.sleep(1000)
	//		}
	//	}
}