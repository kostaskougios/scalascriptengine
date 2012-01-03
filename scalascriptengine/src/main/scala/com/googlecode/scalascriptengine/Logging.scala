package com.googlecode.scalascriptengine
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * logging is done via slf4j
 *
 * @author kostantinos.kougios
 *
 * 25 Dec 2011
 */
protected trait Logging {
	private val logger: Logger = LoggerFactory.getLogger(getClass)

	def debug(msg: => String) = if (logger.isDebugEnabled) logger.debug(msg)
	def info(msg: => String) = if (logger.isInfoEnabled) logger.info(msg)
	def error(msg: String) = logger.error(msg)
	def error(msg: String, e: Throwable) = logger.error(msg, e)

}