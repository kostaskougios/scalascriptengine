package com.googlecode.scalascriptengine

import java.io.File

import com.googlecode.scalascriptengine.classloading.ClassRegistry
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class ClassRegistrySuite extends FunSuite with Matchers
{
	val sourceDir = new File("testfiles/ScalaClassLoaderSuite/v1")

	test("loads classes") {
		val registry = new ClassRegistry(getClass.getClassLoader, Set(sourceDir))
		registry.allClasses.map(_.getName).toSet should be(Set("test.TestDep", "test.TestParam", "test.Test"))
	}
}
