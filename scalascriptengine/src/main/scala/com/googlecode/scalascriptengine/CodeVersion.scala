package com.googlecode.scalascriptengine
import java.io.File

/**
 * @author kostantinos.kougios
 *
 * 24 Dec 2011
 */
case class CodeVersion(val version: Int, val files: Set[SourceFile], classLoader: ScalaClassLoader, val sourceFiles: Map[File, SourceFile]) {
	def get[T](className: String): Class[T] = classLoader.get(className)
	def newInstance[T](className: String): T = classLoader.newInstance(className)

	def isModifiedOrNew(f: File) = sourceFiles.get(f).map(_.lastModified).map(_ != f.lastModified).getOrElse(true)
}

case class SourceFile(file: File, lastModified: Long)