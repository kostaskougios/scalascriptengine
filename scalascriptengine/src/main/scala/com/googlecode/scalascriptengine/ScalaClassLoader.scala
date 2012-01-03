package com.googlecode.scalascriptengine

import java.io.File
import java.net.URL
import java.net.URLClassLoader

/**
 * a throwaway classloader that keeps one version of the source code. For every code change/refresh,
 * a new instance of this classloader is used.
 *
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaClassLoader(sourceDirs: Set[File], classPath: Set[File], parentClassLoader: ClassLoader)
		extends URLClassLoader(classPath.toArray.map(_.toURI.toURL), parentClassLoader) {

	def this(sourceDirs: Set[File], classPath: Set[File]) = this(sourceDirs, classPath, Thread.currentThread.getContextClassLoader)
	def this(sourceDir: File, classPath: Set[File]) = this(Set(sourceDir), classPath)

	// class cache
	private case class Cached(clz: Class[_], srcFile: File, srcDir: File, lastModified: Long)
	private val cache = sourceDirs.map(dir => loadFromDir(dir, dir)).flatten.toMap

	override protected def loadClass(name: String, resolve: Boolean): Class[_] =
		try {
			parentClassLoader.loadClass(name)
		} catch {
			case _: ClassNotFoundException =>
				cache.getOrElse(name, throw new ClassNotFoundException(name)).clz
		}

	def get[T](className: String): Class[T] = cache.getOrElse(className, throw new ClassNotFoundException(className)).clz.asInstanceOf[Class[T]]
	def newInstance[T](className: String): T = get(className).newInstance.asInstanceOf[T]

	private def loadFromDir(srcDir: File, subDir: File): Array[(String, Cached)] = {
		val files = subDir.listFiles
		val fcd = files.filter(_.getName.endsWith(".class")).map(f => loadClassFile(srcDir, f))
		val fsd = files.filter(_.isDirectory).flatMap(dir => loadFromDir(srcDir, dir))
		fcd ++ fsd
	}

	private def loadClassFile(srcDir: File, f: File) = {
		val bytes = Utils.toBytes(f)
		val fp = f.getAbsolutePath
		val className = fp.substring(srcDir.getAbsolutePath.length + 1, fp.length - 6).replace("/", ".")
		val lastModified = f.lastModified
		val clz = defineClass(className, bytes, 0, bytes.length)
		(className, Cached(clz, f, srcDir, lastModified))
	}
}
