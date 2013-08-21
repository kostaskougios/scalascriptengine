package com.googlecode.scalascriptengine

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class ClassRegistrySuite extends FunSuite with ShouldMatchers
{
	val sourceDir = new File("testfiles/ScalaClassLoaderSuite/v1")

	test("loads classes") {
		val registry = new ClassRegistry(List(sourceDir))
		registry.allClasses.size should be(3)

		registry.allClasses.filter(classOf[TestParamTrait].isAssignableFrom _).size should be(1)
	}
}
