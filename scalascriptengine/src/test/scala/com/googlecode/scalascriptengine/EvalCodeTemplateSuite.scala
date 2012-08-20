package com.googlecode.scalascriptengine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 20 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class EvalCodeTemplateSuite extends FunSuite with ShouldMatchers {

	test("using functions") {
		val ect = new EvalCode[String => Int]("s" :: Nil, "s.toInt")

		val x = ect.newInstance
		x("17") should be === 17
	}

	test("constructs src code correctly, 2 args") {
		val ect = new EvalCode[(Float, Double) => Double]("i1" :: "i2" :: Nil, "i1 + i2")
		val x = ect.newInstance
		x(12.5f, 2.5) should be === 15.0
	}

	test("return type string") {
		val ect = new EvalCode[(Float, Double) => String]("i1" :: "i2" :: Nil, "(i1 + i2).toString")
		val x = ect.newInstance
		x(12.5f, 2.5) should be === "15.0"
	}
}

