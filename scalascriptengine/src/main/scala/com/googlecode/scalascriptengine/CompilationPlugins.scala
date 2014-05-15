package com.googlecode.scalascriptengine

import scala.tools.nsc.Global
import scala.tools.nsc.Phase

/**
 * compiler plugins to log and also
 * provide a stop policy
 *
 * @author kostantinos.kougios
 *
 *         8 Jan 2012
 */
class CompilationPlugins(val global: Global, sse: ScalaScriptEngine)
{
	def newPhase(prev: Phase) = new PrePhase(prev, prev.next)

	class PrePhase(prev: Phase, nxt: Phase) extends global.GlobalPhase(prev) with Logging
	{
		if (prev == null) throw new IllegalArgumentException("prev can't be null")
		if (nxt == null) throw new IllegalArgumentException("nxt can't be null")

		override def name = "sse-compilation-plugins"

		override def next = nxt

		override def apply(unit: global.CompilationUnit) {
			info("compiling unit " + unit)
			sse.compilationStatus.checkStop
		}
	}

}
