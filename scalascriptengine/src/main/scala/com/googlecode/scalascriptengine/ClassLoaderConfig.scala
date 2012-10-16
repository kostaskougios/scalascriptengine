package com.googlecode.scalascriptengine

/**
 * @author kostantinos.kougios
 *
 * 11 Oct 2012
 */
case class ClassLoaderConfig(
		protectPackages: Set[String],
		protectClasses: Set[String],
		allowed: String => Boolean) {
	val protectPackagesSuffixed = protectPackages.map(_ + ".")
}

object ClassLoaderConfig {
	def default = ClassLoaderConfig(Set(), Set(), s => true)
}

