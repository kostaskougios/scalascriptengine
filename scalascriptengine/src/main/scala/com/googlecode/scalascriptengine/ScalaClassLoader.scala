package com.googlecode.scalascriptengine

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.security.CodeSource
import java.io.FilePermission

/**
 * a throwaway classloader that keeps one version of the source code. For every code change/refresh,
 * a new instance of this classloader is used.
 *
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
class ScalaClassLoader(
	sourceDirs: Set[File],
	classPath: Set[File],
	parentClassLoader: ClassLoader,
	config: ClassLoaderConfig)
		extends URLClassLoader(
			(classPath ++ sourceDirs).toArray.map(_.toURI.toURL),
			parentClassLoader) {

	def get[T](className: String): Class[T] = loadClass(className).asInstanceOf[Class[T]]
	def newInstance[T](className: String): T = get[T](className).newInstance

	override def loadClass(name: String) = {
		if (!config.protectPackages.isEmpty) {
			val pckg = name.substring(name.lastIndexOf("."))
			config.protectPackages.find(_ == pckg).map {
				throw new IllegalAccessException("access to package " + pckg + " not allowed")
			}
		}
		super.loadClass(name)
	}
}
