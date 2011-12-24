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

	def load[T](sourceDir: File, className: String): Class[T] = {
		val scalaFile = sourceDir.getAbsolutePath + "/" + className.replace('.', '/') + ".scala"
		compileManager.compile(Set(scalaFile))
		classLoader.loadClass(className).asInstanceOf[Class[T]]
	}

	def newInstance[T](sourceDir: File, className: String): T = {
		val clz = load(sourceDir, className)
		clz.newInstance
	}

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