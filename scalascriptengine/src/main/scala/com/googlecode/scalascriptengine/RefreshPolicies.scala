package com.googlecode.scalascriptengine

import org.scala_tools.time.Imports._
import com.googlecode.concurrent.ExecutorServiceManager
import java.io.File

/**
 * periodically scans the source directories and if a file changed, it recompiles
 * and creates a new CodeVersion (changes will be visible as soon as compilation
 * completes)
 *
 * @author kostantinos.kougios
 *
 * 25 Dec 2011
 */
trait TimedRefresh { this: ScalaScriptEngine =>
	def rescheduleAt: DateTime

	private val executor = ExecutorServiceManager.newScheduledThreadPool(1, e => error("error during recompilation of %s".format(this), e))
	executor.runPeriodically(rescheduleAt, Some(rescheduleAt)) {
		refresh
	}

	def shutdown = executor.shutdown
}
