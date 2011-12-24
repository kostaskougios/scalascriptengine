package com.googlecode.scalascriptengine

import java.io.File

/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaClassLoader(sourceDirs: Set[File], classPath: Set[File], parentClassLoader: ClassLoader) extends ClassLoader {
	def this(sourceDirs: Set[File], classPath: Set[File]) = this(sourceDirs, classPath, Thread.currentThread.getContextClassLoader)
	def this(sourceDir: File, classPath: Set[File]) = this(Set(sourceDir), classPath)

	// class cache
	private case class Cached(clz: Class[_], srcFile: File, srcDir: File, lastModified: Long)
	@volatile private var cache = Map[String, Cached]()

	override protected def loadClass(name: String, resolve: Boolean): Class[_] =
		try {
			parentClassLoader.loadClass(name)
		} catch {
			case _: ClassNotFoundException =>
				cache.getOrElse(name, throw new ClassNotFoundException(name)).clz
		}

	def getClass[T](className: String): Class[T] = cache.getOrElse(className, throw new ClassNotFoundException(className)).clz.asInstanceOf[Class[T]]
	def newInstance[T](className: String): T = getClass(className).newInstance.asInstanceOf[T]

	def refresh: ClassLoader = {
		val loader = new ThrowawayClassLoader
		val all = sourceDirs.map(dir => loadFromDir(dir, dir, loader)).flatten
		cache = all.toMap
		loader
	}

	private def loadFromDir(srcDir: File, subDir: File, loader: ThrowawayClassLoader): Array[(String, Cached)] = {
		val files = subDir.listFiles
		val fcd = files.filter(_.getName.endsWith(".class")).map(f => loadClassFile(loader, srcDir, f))
		val fsd = files.filter(_.isDirectory).flatMap(dir => loadFromDir(srcDir, dir, loader))
		fcd ++ fsd
	}

	private def loadClassFile(loader: ThrowawayClassLoader, srcDir: File, f: File) = {
		val bytes = Utils.toBytes(f)
		val fp = f.getAbsolutePath
		val className = fp.substring(srcDir.getAbsolutePath.length + 1, fp.length - 6).replace("/", ".")
		val lastModified = f.lastModified
		val clz = loader.get(className, bytes)
		(className, Cached(clz, f, srcDir, lastModified))
	}
}

private class ThrowawayClassLoader extends ClassLoader {
	def get(name: String, bytes: Array[Byte]) = {
		//		val cs = new CodeSource(f.toURI.toURL, Array[java.security.cert.Certificate]())
		//		val pd = new ProtectionDomain(cs, null, loader, Array[java.security.Principal]())
		defineClass(name, bytes, 0, bytes.length)
	}
}