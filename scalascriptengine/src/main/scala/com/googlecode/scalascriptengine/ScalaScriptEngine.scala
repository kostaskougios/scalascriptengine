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
	@volatile private var codeVersion: CodeVersion = new CodeVersion {
		override def version: Int = 0
		override def classLoader: ScalaClassLoader = throw new IllegalStateException("CodeVersion not yet ready.")
		override def files: Set[SourceFile] = Set()
		override def sourceFiles = Map[File, SourceFile]()
		override def get[T](className: String): Class[T] = throw new IllegalStateException("CodeVersion not yet ready.")
		override def newInstance[T](className: String): T = throw new IllegalStateException("CodeVersion not yet ready.")
		override def isModifiedOrNew(f: File) = true
	}

	def currentVersion = codeVersion
	def versionNumber = codeVersion.version

	def refresh: CodeVersion = {

		def refresh0(srcDir: File): Set[File] = {
			val files = srcDir.listFiles
			val scalaFiles = files.filter(f => f.getName.endsWith(".scala") && codeVersion.isModifiedOrNew(f))
			val rest = files.filter(_.isDirectory).map(refresh0 _).flatten
			(scalaFiles ++ rest).toSet
		}

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
					if (versionNumber > 0) {
						// update fileset to this codeversion to avoid
						// continuously compiling problematic code
						codeVersion = CodeVersionImpl(
							codeVersion.version,
							sourceFilesSet,
							codeVersion.classLoader,
							sourceFiles)
					}
					throw e
			}
			val classLoader = new ScalaClassLoader(outputDir, sourcePaths ++ classLoadingClassPaths)
			debug("done refreshing")
			codeVersion = CodeVersionImpl(
				codeVersion.version + 1,
				sourceFilesSet,
				classLoader,
				sourceFiles)
			codeVersion
		}
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

	/**
	 * creates a ScalaScriptEngine with the following behaviour:
	 *
	 * if a change in a requested class source file is detected, the source path
	 * will be recompiled (this includes all changed files that changed). For each
	 * call to ScalaScriptEngine.get(className), the filesystem is checked for
	 * modifications in the relevant scala file. For a more efficient way of
	 * doing the same in production environments, please look at
	 * #onChangeRefresh(sourcePath, recheckEveryInMillis)
	 *
	 * @param sourcePath	the path where the scala source files are.
	 * @return				the ScalaScriptEngine
	 */
	def onChangeRefresh(sourcePath: File): ScalaScriptEngine with OnChangeRefresh =
		onChangeRefresh(sourcePath, 0)

	/**
	 * creates a ScalaScriptEngine with the following behaviour:
	 *
	 * if a change in a requested class source file is detected, the source path
	 * will be recompiled (this includes all changed files that changed). For each
	 * call to ScalaScriptEngine.get(className), provided that recheckEveryInMillis
	 * has passed between the call and the previous filesystem check,
	 * the filesystem is checked for
	 * modifications in the relevant scala file.
	 *
	 * @param sourcePath			the path where the scala source files are.
	 * @param recheckEveryInMillis	each file will only be checked for changes
	 * 								once per recheckEveryInMillis milliseconds.
	 * @return						the ScalaScriptEngine
	 */
	def onChangeRefresh(sourcePath: File, recheckEveryInMillis: Long): ScalaScriptEngine with OnChangeRefresh =
		new ScalaScriptEngine(
			Set(sourcePath),
			currentClassPath,
			Set(),
			tmpFolder) with OnChangeRefresh with RefreshSynchronously {
			val recheckEveryMillis: Long = recheckEveryInMillis
		}

	/**
	 * similar to onChangeRefresh, but the compilation occurs in the background.
	 * While the compilation occurs, the ScalaScriptEngine.get(className)
	 * returns the existing version of the class without blocking.
	 */
	def onChangeRefreshAsynchronously(sourcePath: File): ScalaScriptEngine with OnChangeRefresh with RefreshAsynchronously = onChangeRefreshAsynchronously(sourcePath, 0)

	/**
	 * similar to onChangeRefresh, but the compilation occurs in the background.
	 * While the compilation occurs, the ScalaScriptEngine.get(className)
	 * returns the existing version of the class without blocking.
	 */
	def onChangeRefreshAsynchronously(sourcePath: File, recheckEveryInMillis: Long): ScalaScriptEngine with OnChangeRefresh with RefreshAsynchronously =
		new ScalaScriptEngine(
			Set(sourcePath),
			currentClassPath,
			Set(),
			tmpFolder) with OnChangeRefresh with RefreshAsynchronously {
			val recheckEveryMillis: Long = recheckEveryInMillis
		}
}