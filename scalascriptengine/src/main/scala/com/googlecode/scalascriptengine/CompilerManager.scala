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
 *         22 Dec 2011
 */
protected class CompilerManager(sourcePaths: List[SourcePath], classPaths: Set[File], sse: ScalaScriptEngine) extends Logging {

	def compile(allFiles: List[String]) = {

		def doCompile(sp: SourcePath, cp: Set[File]) {
			val settings = new Settings(s => {
				error("errors report: " + s)
			})
			settings.sourcepath.tryToSet(sp.sourceDir.getAbsolutePath :: Nil)
			settings.classpath.tryToSet(List(cp.map(_.getAbsolutePath).mkString(File.pathSeparator)))
			settings.outdir.tryToSet(sp.targetDir.getAbsolutePath :: Nil)

			val g = new Global(settings, new CompilationReporter)
			val run = new g.Run

			val phase = run.phaseNamed("typer")
			val cps = new CompilationPlugins(g, sse)
			cps.Component.newPhase(phase)

			val rootPath = sp.sourceDir.getAbsolutePath
			val files = allFiles.filter(_.startsWith(rootPath))
			run.compile(files)
		}

		def all(todo: List[SourcePath], done: List[SourcePath]) {
			todo match {
				case Nil =>
				// nop
				case h :: t =>
					doCompile(h, classPaths ++ done.map(_.targetDir))
					all(t, h :: done)
			}
		}

		all(sourcePaths, Nil)
	}

}

class CompilationError(msg: String) extends RuntimeException(msg)

import scala.tools.nsc.util.Position

private class CompilationReporter extends Reporter with Logging {
	protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean): Unit = {
		error(msg)
		if (severity == ERROR)
			throw new CompilationError("error during compilation : %s".format(msg))
	}

	override def hasErrors: Boolean = false
}