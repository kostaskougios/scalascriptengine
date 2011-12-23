package com.googlecode.scalascriptengine
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import scala.tools.nsc.symtab.classfile.ClassfileParser
/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaClassLoader(sourceDirs: Set[File], parentClassLoader: ClassLoader) extends ClassLoader {

	private val cnt = new AtomicInteger

	def this(urls: Set[File]) = this(urls, Thread.currentThread.getContextClassLoader)

	override protected def loadClass(name: String, resolve: Boolean): Class[_] =
		try {
			parentClassLoader.loadClass(name)
		} catch {
			case _: ClassNotFoundException =>
				findClass(name)
		}

	override protected def findClass(name: String): Class[_] = {
		val fname = name.replace(".", "/") + ".class"
		val srcF = sourceDirs.map(dir => new File(dir, fname)).find(_.exists).getOrElse(throw new ClassNotFoundException(name))
		val bytes = Utils.toBytes(srcF)
		new ThrowawayClassLoader(bytes).get(name)
	}

	def newInstance[T](className: String): T = {
		val clz = loadClass(className)
		clz.newInstance.asInstanceOf[T]
	}
}

private class ThrowawayClassLoader(bytes: Array[Byte]) extends ClassLoader {
	def get(name: String) = defineClass(name, bytes, 0, bytes.length)
}