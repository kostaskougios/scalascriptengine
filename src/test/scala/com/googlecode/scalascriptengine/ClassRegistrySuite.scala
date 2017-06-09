package com.googlecode.scalascriptengine

import java.io.File

import com.googlecode.scalascriptengine.classloading.ClassRegistry
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
 * @author kkougios
 */
class ClassRegistrySuite extends FunSuite
{
	val sourceDir = new File("testfiles/ScalaClassLoaderSuite/v1")

	test("loads classes") {
		val registry = new ClassRegistry(getClass.getClassLoader, Set(sourceDir))
		registry.allClasses.map(_.getName).toSet should be(Set("test.TestDep", "test.TestParam", "test.Test"))
	}
}
