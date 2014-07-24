package com.googlecode.scalascriptengine.classloading

/**
 * @author kostantinos.kougios
 *
 *         11 Oct 2012
 */
case class ClassLoaderConfig(
	protectPackages: Set[String] = Set(),
	protectClasses: Set[String] = Set(),
	// a function of (packageName , fullClassName)=> allow access?
	allowed: (String, String) => Boolean = (_, _) => true,
	// register listeners for class loading events, (className,class)=>Unit
	classLoadingListeners: List[(String, Class[_]) => Unit] = Nil,
	enableClassRegistry: Boolean = false
	)
{
	val protectPackagesSuffixed = protectPackages.map(_ + ".")
}

object ClassLoaderConfig
{
	val Default = ClassLoaderConfig()
}

