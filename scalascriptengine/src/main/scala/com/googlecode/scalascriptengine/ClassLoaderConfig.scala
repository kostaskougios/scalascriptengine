package com.googlecode.scalascriptengine

/**
 * @author kostantinos.kougios
 *
 * 11 Oct 2012
 */
case class ClassLoaderConfig(
		protectPackages: Set[String],
		protectClasses: Set[String],
		allowedPackages: Set[String],
		allowedClasses: Set[String]) {
	val protectPackagesSuffixed = protectPackages.map(_ + ".")
	val allowedPackagesSuffixed = allowedPackages.map(_ + ".")
}

object ClassLoaderConfig {
	def default = ClassLoaderConfig(Set(), Set(), Set(), Set())
}