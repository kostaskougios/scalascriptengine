package com.googlecode.scalascriptengine

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.security.CodeSource
import java.io.FilePermission
import java.security.AccessControlException

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

		def accessForbidden() = throw new AccessControlException("access to class " + name + " not allowed")

		if (!config.protectPackages.isEmpty) {
			config.protectPackagesSuffixed.find(name.startsWith(_)).foreach { _ =>
				accessForbidden()
			}
		}
		if (!config.protectClasses.isEmpty) {
			config.protectClasses.find(_ == name).foreach { _ =>
				accessForbidden()
			}
		}
		val pckg = name.substring(0, name.lastIndexOf('.'))
		if (!config.allowed(pckg, name))
			accessForbidden()
		super.loadClass(name)
	}
}
