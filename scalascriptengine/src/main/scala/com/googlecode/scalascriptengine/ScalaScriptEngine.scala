package com.googlecode.scalascriptengine
import java.io.File
import java.util.UUID

/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaScriptEngine private (
		sourcePaths: Set[File],
		classPaths: Set[File],
		val outputDir: File) {
	private def compileManager = new CompilerManager(sourcePaths, classPaths, outputDir)
	private val classLoader = new ScalaClassLoader(outputDir, classPaths)

	def refresh: CodeVersion = {
		val allFiles = sourcePaths.map(srcDir => refresh0(srcDir)).flatten
		val sourceFiles = allFiles.map(f => SourceFile(f, f.lastModified))
		compileManager.compile(allFiles.map(_.getAbsolutePath))
		val tcl = classLoader.loadAll
		CodeVersion(sourceFiles, tcl)
	}

	private def refresh0(srcDir: File): Set[File] = {
		val files = srcDir.listFiles
		val scalaFiles = files.filter(_.getName.endsWith(".scala"))
		val rest = files.filter(_.isDirectory).map(refresh0 _).flatten
		(scalaFiles ++ rest).toSet
	}

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