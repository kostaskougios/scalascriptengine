package com.googlecode.scalascriptengine
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.Global
import scala.tools.nsc.Phase

/**
 * compiler plugins to keep track of dependencies, to log and also
 * provide a stop policy
 *
 * @author kostantinos.kougios
 *
 * 8 Jan 2012
 */
class CompilationPlugins(global: Global, sse: ScalaScriptEngine) {
	import global._

	val name = "compilation-plugins"
	val description = "scalascriptengine compilation plugins"
	val components = List[PluginComponent](Component)

	/**
	 * this wrapping code is not really necessary for our purposes, as we could use
	 * the Phases straight away
	 */
	object Component extends PluginComponent {
		val global: CompilationPlugins.this.global.type = CompilationPlugins.this.global
		val runsAfter = List("typer")
		val phaseName = CompilationPlugins.this.name
		def newPhase(prev: Phase) = new PrePhase(prev, prev.next)

		class PrePhase(prev: Phase, nxt: Phase) extends StdPhase(prev) with Logging {
			override def name = CompilationPlugins.this.name
			override def next = nxt
			def apply(unit: CompilationUnit) {
				info("compiling unit " + unit)
				sse.compilationStatus.checkStop
			}
		}
	}
}