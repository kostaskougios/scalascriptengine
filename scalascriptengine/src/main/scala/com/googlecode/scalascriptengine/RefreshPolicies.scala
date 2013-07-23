package com.googlecode.scalascriptengine

import org.scala_tools.time.Imports._
import com.googlecode.concurrent.ExecutorServiceManager
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * periodically scans the source directories and if a file changed, it recompiles
 * and creates a new CodeVersion (changes will be visible as soon as compilation
 * completes)
 *
 * @author kostantinos.kougios
 *
 *         25 Dec 2011
 */
trait TimedRefresh
{
	this: ScalaScriptEngine =>
	def rescheduleAt: DateTime

	private val executor = ExecutorServiceManager.newScheduledThreadPool(1, e => error("error during recompilation of a source file", e))
	executor.runPeriodically(rescheduleAt, Some(rescheduleAt)) {
		refresh
	}

	def shutdown = executor.shutdown
}

/**
 * checks scala files for modification and if yes it recompiles
 * the changed sources. This is not to be used by client code but
 * rather be used by the rest of the refresh policy traits.
 *
 * recheckEveryMillis should be provided. If <=0 then for every
 * request for a class, the source file of the class is checked
 * for modifications. If >0 then maximum 1 check will be performed
 * every recheckEveryMillis milliseconds. A sensible value might be
 * 1000 millis if code changes frequently (i.e. during dev) and
 * 30000 millis if code doesn't change that often (i.e. production)
 */
protected trait OnChangeRefresh extends ScalaScriptEngine
{
	val recheckEveryMillis: Long
	private val lastChecked = new ConcurrentHashMap[String, java.lang.Long]
	private val timesTested = new AtomicLong

	def numberOfTimesSourcesTestedForModifications = timesTested.get

	abstract override def get[T](className: String): Class[T] = {
		val l = lastChecked.get(className)
		val now = System.currentTimeMillis
		if (l == null || recheckEveryMillis <= 0 || now - l > recheckEveryMillis) {
			lastChecked.put(className, now)
			val fileName = className.replace('.', '/') + ".scala"
			val isModO = config.sourcePaths.find {
				paths =>
					new File(paths.sourceDir, fileName).exists
			}.map {
				paths =>
					isModified(paths, className)
			}
			timesTested.incrementAndGet
			if (isModO.isDefined && isModO.get) doRefresh
		}
		super.get(className)
	}

	def doRefresh: Unit
}

/**
 * refresh as soon as a modification is detected. The first thread that actually
 * does the refresh will do the compilation and the rest of the threads will
 * wait. All threads will get an up to date compiled version of the source code.
 *
 * This is blocking during compilation and is not recommended to be used by
 * web servers. RefreshAsynchronously offers a much better alternative.
 */
trait RefreshSynchronously extends ScalaScriptEngine with OnChangeRefresh
{
	private var lastCompiled: Long = 0

	override def doRefresh: Unit = {
		// refresh only if not already refreshing
		val time = System.currentTimeMillis
		synchronized {
			if (time > lastCompiled) try {
				refresh
			} finally {
				// set lastCompile even in case of compilation errors
				lastCompiled = System.currentTimeMillis
			}
		}
	}
}

/**
 * makes sure the refresh is run only once at a time. All calls
 * to refresh return straight away with the current code version but
 * a compilation will be triggered if the source code changed. The
 * compilation will occur in the background and when done, the new
 * compiled version of the code will be used.
 */
trait RefreshAsynchronously extends ScalaScriptEngine with OnChangeRefresh
{
	private val isCompiling = new AtomicBoolean(false)
	private val executor = ExecutorServiceManager.newSingleThreadExecutor

	override def doRefresh: Unit = {
		// refresh only if not already refreshing
		val c = isCompiling.getAndSet(true)
		if (!c) executor.submit {
			try {
				refresh
			} catch {
				case e: Throwable =>
					error("error during refresh", e)
			} finally {
				isCompiling.set(false)
			}
		}
	}

	def shutdown = executor.shutdown
}