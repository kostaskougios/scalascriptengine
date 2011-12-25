package com.googlecode.scalascriptengine
import java.io.File
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaScriptEngine private (
		sourcePaths: Set[File],
		classPaths: Set[File],
		val outputDir: File) extends Logging {

	private def compileManager = new CompilerManager(sourcePaths, classPaths, outputDir)
	private val classLoader = new ScalaClassLoader(outputDir, classPaths)
	private var sourceFiles = Map[File, SourceFile]()

	def refresh: CodeVersion = sourceFiles.synchronized {
		val allFiles = sourcePaths.map(srcDir => refresh0(srcDir)).flatten
		debug("refreshing %s".format(allFiles))
		var sourceFilesSet = allFiles.map(f => SourceFile(f, f.lastModified))
		sourceFiles = sourceFilesSet.map(s => (s.file, s)).toMap
		compileManager.compile(allFiles.map(_.getAbsolutePath))
		val tcl = classLoader.refresh
		CodeVersion(sourceFilesSet, tcl)
	}

	private def refresh0(srcDir: File): Set[File] = {
		val files = srcDir.listFiles
		val scalaFiles = files.filter(_.getName.endsWith(".scala"))
		val rest = files.filter(_.isDirectory).map(refresh0 _).flatten
		(scalaFiles ++ rest).toSet
	}

	def get[T](className: String): Class[T] = classLoader.getClass(className)
	def newInstance[T](className: String): T = classLoader.newInstance(className)

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
		val dir = new File(System.getProperty("java.io.tmpdir"), "scala-script-engine-classes-" + UUID.randomUUID())
		dir.mkdir
		dir
	}
	def apply(sourcePaths: Set[File], classPaths: Set[File]) = new ScalaScriptEngine(sourcePaths, classPaths, tmpFolder)
	def apply(sourcePath: File, classPaths: Set[File]) = new ScalaScriptEngine(Set(sourcePath), classPaths, tmpFolder)
}