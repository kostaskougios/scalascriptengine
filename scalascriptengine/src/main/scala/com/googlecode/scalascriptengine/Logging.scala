package com.googlecode.scalascriptengine

import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * logging is done via slf4j
 *
 * @author kostantinos.kougios
 *
 *         25 Dec 2011
 */
protected trait Logging
{
	private val logger: Logger = LoggerFactory.getLogger(getClass)

	protected def debug(msg: => String) = if (logger.isDebugEnabled) logger.debug(msg)

	protected def info(msg: => String) = if (logger.isInfoEnabled) logger.info(msg)

	protected def warn(msg: String) = logger.warn(msg)

	protected def error(msg: String) = logger.error(msg)

	protected def error(msg: String, e: Throwable) = logger.error(msg, e)
}