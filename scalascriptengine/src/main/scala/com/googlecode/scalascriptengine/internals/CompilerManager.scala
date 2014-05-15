package com.googlecode.scalascriptengine.internals

import scala.reflect.internal.util.Position
import java.io.File

import scala.tools.nsc.reporters.AbstractReporter
import scala.tools.nsc.{Phase, SubComponent, Settings, Global}
import com.googlecode.scalascriptengine.{Logging, ScalaScriptEngine, SourcePath}

/**
 * manages the scala compiler, taking care of setting the correct compiler parameters
 * and reporting errors.
 *
 * @author kostantinos.kougios
 *
 *         22 Dec 2011
 */
class CompilerManager(sourcePaths: List[SourcePath], classPaths: Set[File], sse: ScalaScriptEngine) extends Logging
{

	private def acc(todo: List[SourcePath], done: List[SourcePath]): List[(SourcePath, (Global, Global#Run, CompilationReporter))] = todo match {
		case Nil => Nil
		case h :: t =>
			val settings = new Settings(s => {
				error("errors report: " + s)
			})
			settings.sourcepath.tryToSet(h.sources.map(_.getAbsolutePath).toList)
			val cp = done.map(_.targetDir) ++ classPaths
			settings.classpath.tryToSet(List(cp.map(_.getAbsolutePath).mkString(File.pathSeparator)))
			settings.outdir.tryToSet(h.targetDir.getAbsolutePath :: Nil)

			val reporter = new CompilationReporter(settings)

			class SSEGlobal extends Global(settings, reporter)
			{

				object SSEPhase extends SubComponent
				{
					val global: SSEGlobal.this.type = SSEGlobal.this
					val phaseName = "SSEPhase"
					val runsAfter = List[String]("typer")
					val runsRightAfter = None

					def newPhase(_prev: Phase): StdPhase = new StdPhase(_prev)
					{
						def apply(unit: CompilationUnit) {
							info("compiling unit " + unit)
							sse.compilationStatus.checkStop
						}
					}
				}

				override protected def computePhaseDescriptors = {
					addToPhasesSet(SSEPhase, "SSEPhase")
					super.computePhaseDescriptors
				}
			}

			val g = new SSEGlobal

			val run = new g.Run
			(h, (g, run, reporter)) :: acc(t, h :: done)

	}

	private val runMap = acc(sourcePaths, Nil).toMap

	def compile(allFiles: List[String]) = {

		def doCompile(sp: SourcePath, cp: Set[File]) {
			val (g, run, reporter) = runMap(sp)

			val rootPaths = sp.sources.map(_.getAbsolutePath)
			val files = allFiles.filter {
				f =>
					rootPaths.exists(rp => f.startsWith(rp))
			}
			run.compile(files)

			val errors = reporter.errors.result
			if (!errors.isEmpty) throw new CompilationError(s"${errors.size} error(s) occured :\n${errors.mkString("\n")}")
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

private class CompilationReporter(val settings: Settings) extends AbstractReporter with Logging
{
	val errors = List.newBuilder[String]

	def display(pos: Position, msg: String, severity: Severity) {
		val m = Position.formatMessage(pos, msg, true)
		if (severity == ERROR)
			errors += m
		else warn(m)
	}

	def displayPrompt() {

	}
}