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

	test("constructs src code correctly") {
		trait X { def apply(s: String): Int }

		val ect = new EvalCode[X]("s" :: Nil, "s.toInt")

		val x = ect.newInstance
		x("17") should be === 17
	}
}

