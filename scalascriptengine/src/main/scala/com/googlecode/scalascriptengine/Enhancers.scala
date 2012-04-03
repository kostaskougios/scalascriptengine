package com.googlecode.scalascriptengine

/**
 * if no code version is ready,
 * tries to find the classes from the classpath initially. If that
 * fails, it then goes the route of finding/compiling a scala class
 * from the source classpaths.
 *
 * If a codeversion is ready, it just uses the codeversion
 *
 * This can come handy i.e. during development if your IDE compiles
 * the dynamic scala classes and you wouldn't like to wait for the
 * classes to be recompiled.
 *
 * @author kostantinos.kougios
 *
 * 2 Jan 2012
 */
trait FromClasspathFirst extends ScalaScriptEngine {
	abstract override def get[T](className: String): Class[T] =
		if (currentVersion.version == 0) {
			try {
				Class.forName(className).asInstanceOf[Class[T]]
			} catch {
				case _: ClassNotFoundException => super.get(className)
			}
		} else super.get(className)
}