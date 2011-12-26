package com.googlecode.scalascriptengine
import java.io.File
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.scala_tools.time.Imports._

/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaScriptEngine(
		protected val sourcePaths: Set[File],
		compilationClassPaths: Set[File],
		classLoadingClassPaths: Set[File],
		val outputDir: File) extends Logging {

	private def compileManager = new CompilerManager(sourcePaths, compilationClassPaths, outputDir)
	@volatile private var codeVersion = CodeVersion(0, Set(), null, Map())

	def currentVersion = codeVersion
	def versionNumber = codeVersion.version

	def refresh: CodeVersion = {
		val allChangedFiles = sourcePaths.map(srcDir => refresh0(srcDir)).flatten
		if (allChangedFiles.isEmpty)
			codeVersion
		else {
			debug("refreshing changed files %s".format(allChangedFiles))
			var sourceFilesSet = allChangedFiles.map(f => SourceFile(f, f.lastModified))

			def sourceFiles = sourceFilesSet.map(s => (s.file, s)).toMap

			try {
				compileManager.compile(allChangedFiles.map(_.getAbsolutePath))
			} catch {
				case e =>
					// update fileset to this codeversion to avoid
					// continuously compiling problematic code
					codeVersion = CodeVersion(
						codeVersion.version,
						sourceFilesSet,
						codeVersion.classLoader,
						sourceFiles)
					throw e
			}
			val classLoader = new ScalaClassLoader(outputDir, classLoadingClassPaths)
			debug("done refreshing")
			codeVersion = CodeVersion(
				codeVersion.version + 1,
				sourceFilesSet,
				classLoader,
				sourceFiles)
			codeVersion
		}
	}

	private def refresh0(srcDir: File): Set[File] = {
		val files = srcDir.listFiles
		val scalaFiles = files.filter(f => f.getName.endsWith(".scala") && codeVersion.isModifiedOrNew(f))
		val rest = files.filter(_.isDirectory).map(refresh0 _).flatten
		(scalaFiles ++ rest).toSet
	}

	def get[T](className: String): Class[T] = codeVersion.get(className)
	def newInstance[T](className: String): T = get[T](className).newInstance

	/**
	 * please make sure outputDir is valid!!!
	 */
	def deleteAllClassesInOutputDirectory {
		def deleteAllClassesInOutputDirectory(dir: File) {
			dir.listFiles.filter(_.getName.endsWith(".class")).foreach(_.delete)
			dir.listFiles.filter(_.isDirectory).foreach(d => deleteAllClassesInOutputDirectory(d))
		}
		deleteAllClassesInOutputDirectory(outputDir)
	}

}

object ScalaScriptEngine {
	private def tmpFolder = {
		val dir = new File(System.getProperty("java.io.tmpdir"), "scala-script-engine-classes")
		dir.mkdir
		dir
	}

	def currentClassPath = System.getProperty("java.class.path").split(File.pathSeparator).map(p => new File(p)).toSet

	def withoutRefreshPolicy(sourcePaths: Set[File],
		compilationClassPaths: Set[File],
		classLoadingClassPaths: Set[File],
		outputDir: File): ScalaScriptEngine =
		new ScalaScriptEngine(sourcePaths, compilationClassPaths, classLoadingClassPaths, outputDir)

	def withoutRefreshPolicy(sourcePaths: Set[File], compilationClassPaths: Set[File]): ScalaScriptEngine =
		new ScalaScriptEngine(sourcePaths, compilationClassPaths, Set(), tmpFolder)

	def withoutRefreshPolicy(sourcePath: File, compilationClassPaths: Set[File]): ScalaScriptEngine =
		new ScalaScriptEngine(Set(sourcePath), compilationClassPaths, Set(), tmpFolder)

	def withoutRefreshPolicy(sourcePaths: Set[File]): ScalaScriptEngine = {
		withoutRefreshPolicy(sourcePaths, currentClassPath)
	}
	def withoutRefreshPolicy(sourcePath: File): ScalaScriptEngine = withoutRefreshPolicy(Set(sourcePath))

	def timedRefresh(sourcePath: File, refreshEvery: () => DateTime): ScalaScriptEngine with TimedRefresh = new ScalaScriptEngine(Set(sourcePath), currentClassPath, Set(), tmpFolder) with TimedRefresh {
		def rescheduleAt = refreshEvery()
	}

	def onChangeRefresh(sourcePath: File) = new ScalaScriptEngine(
		Set(sourcePath),
		currentClassPath,
		Set(),
		tmpFolder) with OnChangeRefresh with RefreshSynchronously {
		val recheckEveryMillis: Long = 0
	}
}