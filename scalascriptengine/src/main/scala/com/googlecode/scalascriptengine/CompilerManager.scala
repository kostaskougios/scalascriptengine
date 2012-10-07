package com.googlecode.scalascriptengine

import tools.nsc._
import java.io.File

import tools.nsc.reporters.Reporter

/**
 * manages the scala compiler, taking care of setting the correct compiler parameters
 * and reporting errors.
 *
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
protected class CompilerManager(sourcePaths: Set[File], classPaths: Set[File], destDir: File, sse: ScalaScriptEngine) extends Logging {
	val settings = new Settings(s => {
		error("errors report: " + s)
	})
	settings.sourcepath.tryToSet(sourcePaths.map(_.getAbsolutePath).toList)
	settings.classpath.tryToSet(List(classPaths.map(_.getAbsolutePath).mkString(File.pathSeparator)))
	settings.outdir.tryToSet(List(destDir.getAbsolutePath))

	private val g = new Global(settings, new CompilationReporter)
	private lazy val run = new g.Run

	def compile(files: Set[String]) = {
		val phase = run.phaseNamed("typer")
		val cps = new CompilationPlugins(g, sse)
		cps.Component.newPhase(phase)
		run.compile(files.toList)
	}

}

class CompilationError(msg: String) extends RuntimeException(msg)

import scala.tools.nsc.util.Position
private class CompilationReporter extends Reporter with Logging {
	protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean): Unit =
		{
			error(msg)
			if (severity == ERROR)
				throw new CompilationError("error during compilation of %s : %s".format(pos.source.path, msg))
		}

	override def hasErrors: Boolean = false
}