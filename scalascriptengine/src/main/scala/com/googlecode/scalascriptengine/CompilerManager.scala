package com.googlecode.scalascriptengine

import tools.nsc._
import java.io.File

import tools.nsc.reporters.Reporter

/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
protected class CompilerManager(sourcePaths: Set[File], classPaths: Set[File], destDir: File) extends Logging {
	val settings = new Settings(s => {
		error("errors report: " + s)
	})
	settings.sourcepath.tryToSet(sourcePaths.map(_.getAbsolutePath).toList)
	settings.classpath.tryToSet(List(classPaths.map(_.getAbsolutePath).mkString(File.pathSeparator)))
	settings.outdir.tryToSet(List(destDir.getAbsolutePath))

	private val g = new Global(settings, new CompilationReporter);
	private lazy val run = new g.Run

	def compile(files: Set[String]) = {
		run.compile(files.toList)
	}

}

class CompilationError(msg: String) extends RuntimeException(msg)

import scala.tools.nsc.util.Position
private class CompilationReporter extends Reporter with Logging {
	protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean): Unit =
		{
			error(msg)
			throw new CompilationError(msg)
		}

	override def hasErrors: Boolean = false
}