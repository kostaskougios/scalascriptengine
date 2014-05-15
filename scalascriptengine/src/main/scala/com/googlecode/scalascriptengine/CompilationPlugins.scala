package com.googlecode.scalascriptengine

import scala.tools.nsc.plugins.PluginComponent
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
class CompilationPlugins(val global: Global, sse: ScalaScriptEngine) extends PluginComponent
{

	val runsAfter = List("typer")
	val phaseName = CompilationPlugins.PhaseName

	def newPhase(prev: Phase) = new PrePhase(prev, prev.next)

	class PrePhase(prev: Phase, nxt: Phase) extends StdPhase(prev) with Logging
	{
		override def name = CompilationPlugins.PhaseName

		override def next = nxt

		def apply(unit: global.CompilationUnit) {
			info("compiling unit " + unit)
			sse.compilationStatus.checkStop
		}
	}

}

object CompilationPlugins
{
	val PhaseName = "sse-compilation-plugins"
}