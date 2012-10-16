package com.googlecode.scalascriptengine

/**
 * @author kostantinos.kougios
 *
 * 11 Oct 2012
 */
case class ClassLoaderConfig(
		protectPackages: Set[String],
		protectClasses: Set[String],
		// a function of (packageName , fullClassName)=> allow access?
		allowed: (String, String) => Boolean) {
	val protectPackagesSuffixed = protectPackages.map(_ + ".")
}

object ClassLoaderConfig {
	def default = ClassLoaderConfig(Set(), Set(), (_, _) => true)
}

