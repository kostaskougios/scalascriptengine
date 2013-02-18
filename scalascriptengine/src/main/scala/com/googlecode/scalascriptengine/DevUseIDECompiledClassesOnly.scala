package com.googlecode.scalascriptengine

/**
 * this is useful during development. If your IDE compiles the classes (and recompiles them),
 * then there is no need for the script engine to recompile those. Just mixin this trait
 * and don't call refresh (if refresh is called then it falls back to normal operation)
 *
 * Note: don't use this on production or stress tests as it will reload the classes over
 * and over again until java runs out of PermGen space.
 *
 * @author: kostas.kougios
 *          Date: 18/02/13
 */
trait DevUseIDECompiledClassesOnly extends ScalaScriptEngine {

	@volatile
	private var cl: ScalaClassLoader = createClassLoader
	@volatile
	private var lastRefresh = System.currentTimeMillis

	@volatile
	var classVersion: Int = 0

	abstract override def get[T](className: String): Class[T] =
		if (currentVersion.version == 0) {
			if (System.currentTimeMillis - lastRefresh > 100) {
				cl = createClassLoader
				lastRefresh = System.currentTimeMillis
				classVersion += 1
			}
			cl.get(className)
		} else super.get(className)
}
