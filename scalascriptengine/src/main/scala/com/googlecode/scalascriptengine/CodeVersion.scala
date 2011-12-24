package com.googlecode.scalascriptengine
import java.io.File

/**
 * @author kostantinos.kougios
 *
 * 24 Dec 2011
 */
case class CodeVersion(val files: Set[SourceFile], val classLoader: ClassLoader)

case class SourceFile(file: File, lastModified: Long)