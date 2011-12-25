package com.googlecode.scalascriptengine
import java.io.File
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory

trait ScalaScriptEngine {
	def get[T](className: String): Class[T]
	def newInstance[T](className: String): T
	def refresh: CodeVersion
	/**
	 * please make sure outputDir is valid!!!
	 */
	def deleteAllClassesInOutputDirectory: Unit

	def outputDir: File
}
/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaScriptEngineImpl(
		sourcePaths: Set[File],
		compilationClassPaths: Set[File],
		classLoadingClassPaths: Set[File],
		val outputDir: File) extends ScalaScriptEngine with Logging {

	private def compileManager = new CompilerManager(sourcePaths, compilationClassPaths, outputDir)
	@volatile private var codeVersion = CodeVersion(0, Set(), null, Map())

	def currentVersion = codeVersion

	def refresh: CodeVersion = {
		val allChangedFiles = sourcePaths.map(srcDir => refresh0(srcDir)).flatten
		if (allChangedFiles.isEmpty)
			codeVersion
		else {
			debug("refreshing changed files %s".format(allChangedFiles))
			var sourceFilesSet = allChangedFiles.map(f => SourceFile(f, f.lastModified))
			compileManager.compile(allChangedFiles.map(_.getAbsolutePath))
			val sourceFiles = sourceFilesSet.map(s => (s.file, s)).toMap
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
	def newInstance[T](className: String): T = codeVersion.newInstance(className)

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
		val dir = new File(System.getProperty("java.io.tmpdir"), "scala-script-engine-classes-" + UUID.randomUUID())
		dir.mkdir
		dir
	}
	def apply(sourcePaths: Set[File],
		compilationClassPaths: Set[File],
		classLoadingClassPaths: Set[File],
		outputDir: File): ScalaScriptEngine =
		new ScalaScriptEngineImpl(sourcePaths, compilationClassPaths, classLoadingClassPaths, outputDir)

	def apply(sourcePaths: Set[File], compilationClassPaths: Set[File]): ScalaScriptEngine =
		new ScalaScriptEngineImpl(sourcePaths, compilationClassPaths, Set(), tmpFolder)

	def apply(sourcePath: File, compilationClassPaths: Set[File]): ScalaScriptEngine =
		new ScalaScriptEngineImpl(Set(sourcePath), compilationClassPaths, Set(), tmpFolder)

	def apply(sourcePaths: Set[File]): ScalaScriptEngine = {
		val compilationClassPaths = System.getProperty("java.class.path").split(File.pathSeparator).map(p => new File(p)).toSet
		apply(sourcePaths, compilationClassPaths)
	}
	def apply(sourcePath: File): ScalaScriptEngine = apply(Set(sourcePath))
}