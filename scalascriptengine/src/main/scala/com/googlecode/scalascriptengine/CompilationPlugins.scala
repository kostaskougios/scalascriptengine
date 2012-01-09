package com.googlecode.scalascriptengine
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.Global
import scala.tools.nsc.Phase

/**
 * @author kostantinos.kougios
 *
 * 8 Jan 2012
 */
class CompilationPlugins(global: Global) {
	import global._

	val name = "compilation-plugins"
	val description = "scalascriptengine compilation plugins"
	val components = List[PluginComponent](Component)

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
			}
		}
	}
}