package com.googlecode.scalascriptengine

import org.scala_tools.time.Imports._
import com.googlecode.concurrent.ExecutorServiceManager

/**
 * periodically scans the source directories and if a file changed, it recompiles
 * and creates a new CodeVersion (changes will be visible as soon as compilation
 * completes)
 *
 * @author kostantinos.kougios
 *
 * 25 Dec 2011
 */
class TimedRefresh(engine: ScalaScriptEngine, every: => DateTime) extends Logging {

	private val executor = ExecutorServiceManager.newScheduledThreadPool(1, e => error("error during recompilation of %s".format(engine), e))
	executor.runPeriodically(every, Some(every)) {
		engine.refresh
	}

	def shutdown = executor.shutdown
}
