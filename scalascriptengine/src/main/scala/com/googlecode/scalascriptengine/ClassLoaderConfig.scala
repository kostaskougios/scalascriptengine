package com.googlecode.scalascriptengine

/**
 * @author kostantinos.kougios
 *
 * 11 Oct 2012
 */
case class ClassLoaderConfig(
		protectPackages: Set[String],
		protectClasses: Set[String]) {
	val protectClassesSuffixed = protectPackages.map(_ + ".")
}

object ClassLoaderConfig {
	def default = ClassLoaderConfig(Set(), Set())
}