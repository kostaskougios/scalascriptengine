package com.googlecode.scalascriptengine

import java.io.{File, FileInputStream}

import scala.language.reflectiveCalls
import scala.reflect.ClassTag

/**
 * finds all class names for a list of directories
 *
 * @author kostas.kougios
 *          Date: 21/08/13
 */
class ClassRegistry(parentClassLoader: ClassLoader, dirs: Set[File])
{
	val allClasses = {
		val classFiles = find(dirs.toList)
		val classLoader = new ClassLoader(parentClassLoader)
		{
			def scan = classFiles.map {
				f =>
					val is = new FileInputStream(f)
					try {
						val cnt = is.available
						val bytes = Array.ofDim[Byte](cnt)
						is.read(bytes)
						defineClass(null, bytes, 0, bytes.length)
					} finally {
						is.close()
					}
			}
		}
		classLoader.scan
	}

	def withTypeOf[T](implicit ct: ClassTag[T]) = allClasses.filter(ct.runtimeClass.isAssignableFrom _)

	// find all class files
	private def find(dirs: List[File]): List[File] = dirs.map {
		dir =>
			if (!dir.isDirectory) throw new IllegalArgumentException(s"not a directory: $dir")
			val files = dir.listFiles.toList
			val subDirs = find(files.filter(_.isDirectory))
			files.filter(_.getName.endsWith(".class")) ::: subDirs
	}.flatten
}
