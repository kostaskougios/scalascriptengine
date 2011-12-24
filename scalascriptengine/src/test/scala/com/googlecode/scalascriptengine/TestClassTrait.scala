package com.googlecode.scalascriptengine

/**
 * inherited by compiled test classes
 *
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
trait TestClassTrait {
	def result: String
}

trait TestParamTrait {
	def result(tct: TestClassTrait): String
}

trait TestValTrait {
	val x: Int
}