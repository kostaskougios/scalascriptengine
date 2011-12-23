package com.googlecode.scalascriptengine
import java.io.File

/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaClassLoader(sourceDirs: Set[File], parentClassLoader: ClassLoader) extends ClassLoader {
	def this(urls: Set[File]) = this(urls, Thread.currentThread.getContextClassLoader)

	override protected def loadClass(name: String, resolve: Boolean): Class[_] =
		try {
			parentClassLoader.loadClass(name)
		} catch {
			case _: ClassNotFoundException =>
				findClass(name)
		}

	override protected def findClass(name: String): Class[_] = {
		val fname = name.replace(".", "/") + ".scala"
		val srcF = sourceDirs.find(dir => new File(dir, fname).exists).getOrElse(throw new ClassNotFoundException(name))
		Utils.toBytes(srcF)
	}

	def newInstance[T](className: String): T = {
		val clz = loadClass(className)
		clz.newInstance.asInstanceOf[T]
	}
}