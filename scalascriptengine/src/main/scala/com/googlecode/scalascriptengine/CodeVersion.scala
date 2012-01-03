package com.googlecode.scalascriptengine
import java.io.File

/**
 * the script engine works by keeping 1 version of the compiled source directories.
 * Every time a script changes, a new compilation is triggered (called a "refresh")
 * and done the current version is replaced by the new version.
 *
 * @author kostantinos.kougios
 *
 * 24 Dec 2011
 */
trait CodeVersion {
	def version: Int
	def classLoader: ScalaClassLoader
	def files: Set[SourceFile]
	def sourceFiles: Map[File, SourceFile]
	def get[T](className: String): Class[T]
	def newInstance[T](className: String): T

	def isModifiedOrNew(f: File): Boolean
}

case class CodeVersionImpl(val version: Int, val files: Set[SourceFile], classLoader: ScalaClassLoader, val sourceFiles: Map[File, SourceFile]) extends CodeVersion {
	override def get[T](className: String): Class[T] = classLoader.get(className)
	override def newInstance[T](className: String): T = classLoader.newInstance(className)

	override def isModifiedOrNew(f: File) = sourceFiles.get(f).map(_.lastModified).map(_ != f.lastModified).getOrElse(true)
}

case class SourceFile(file: File, lastModified: Long)