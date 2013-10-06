package com.googlecode.scalascriptengine

import org.scalatest.{Matchers, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class ClassRegistrySuite extends FunSuite with Matchers
{
	val sourceDir = new File("testfiles/ScalaClassLoaderSuite/v1")

	test("loads classes") {
		val registry = new ClassRegistry(Set(sourceDir))
		registry.allClasses.toSet should be(Set("test.TestDep", "test.TestParam", "test.Test"))
	}
}
