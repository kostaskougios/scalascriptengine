package com.googlecode.scalascriptengine

import com.googlecode.classgenerator.ReflectionManager

/**
 * @author kostantinos.kougios
 *
 * 20 Aug 2012
 */

class EvalCodeTemplate(clz: Class[_]) extends CodeTemplate {

	private val reflectionManager = new ReflectionManager

	val applyMethod = reflectionManager.method(clz, "apply").getOrElse(throw new IllegalArgumentException("class %s doesn't have an apply method".format(clz)))

	private val templateTop = """
		package eval
		
		class Eval extends com.googlecode.scalascriptengine.Eval {
		}
		"""
	override def code(eval: String) = ""
}