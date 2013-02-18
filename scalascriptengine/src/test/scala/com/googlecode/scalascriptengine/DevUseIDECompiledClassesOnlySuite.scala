package com.googlecode.scalascriptengine

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import com.googlecode.scalascriptengine.scalascriptengine._

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class DevUseIDECompiledClassesOnlySuite extends FunSuite with ShouldMatchers {
	val targetDir = new File("testfiles/ScalaClassLoaderSuite")
	// parent classloader will contain scala-lib and all test-compiled classes
	val classPath = Set[File]()

	test("using both v1 and v2 classes") {
		val destDir = newTmpDir("dynamicclass")

		val sse = new ScalaScriptEngine(Config(sourcePaths = List(
			SourcePath(destDir, destDir)
		))) with DevUseIDECompiledClassesOnly

		for (i <- 0 to 9) {
			if(i>0) Thread.sleep(150)
			cleanDestinationAndCopyFromSource(new File(targetDir, "v1"), destDir)
			val tctV1 = sse.newInstance[TestClassTrait]("test.Test")
			val tcpV1 = sse.newInstance[TestParamTrait]("test.TestParam")
			tcpV1.result(tctV1) should be === "TP:v1"

			sse.classVersion should be(i * 2)

			Thread.sleep(150)
			cleanDestinationAndCopyFromSource(new File(targetDir, "v2"), destDir)

			val tcpV2 = sse.newInstance[TestParamTrait]("test.TestParam")
			tcpV2.result(tctV1) should be === "TP:v1"

			val tctV2 = sse.newInstance[TestClassTrait]("test.Test")
			tcpV2.result(tctV2) should be === "TP:v2"
		}
	}
}