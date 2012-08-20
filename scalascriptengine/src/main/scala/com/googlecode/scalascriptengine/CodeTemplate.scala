package com.googlecode.scalascriptengine

/**
 * a code template acts as a src code class factory
 *
 * @author kostantinos.kougios
 *
 * 20 Aug 2012
 */
trait CodeTemplate {
	/**
	 * generates the code which evaluates "eval"
	 */
	def code(eval: String): String
}