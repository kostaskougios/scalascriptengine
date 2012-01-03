package com.googlecode.scalascriptengine

/**
 * tries to find the classes from the classpath initially. If that
 * fails, it then goes the route of finding/compiling a scala class
 * from the source classpaths.
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
		try {
			Class.forName(className).asInstanceOf[Class[T]]
		} catch {
			case _: ClassNotFoundException => super.get(className)
		}
}