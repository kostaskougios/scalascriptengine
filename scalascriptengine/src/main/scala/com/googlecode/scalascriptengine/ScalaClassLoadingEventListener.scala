package com.googlecode.scalascriptengine

/**
 * receives notification events when scala scripts are loaded
 *
 * @author: kostas.kougios
 *          Date: 24/04/13
 */
trait ScalaClassLoadingEventListener
{
	def classLoaded(className: String, clz: Class[_])
}
