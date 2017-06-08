package com.googlecode.concurrent

import java.util.concurrent._

import org.joda.time.DateTime

/**
  * manages executor instantiation, provides factory methods
  * for various executors
  *
  * @author kostantinos.kougios
  *
  *         15 Nov 2011
  */
object ExecutorServiceManager
{

	def wrap(executor: ExecutorService) = new Executor with Shutdown
	{
		protected val executorService = executor
	}

	def newSingleThreadExecutor = new Executor with Shutdown
	{
		protected val executorService = Executors.newSingleThreadExecutor
	}

	def newCachedThreadPool(
		corePoolSize: Int,
		maximumPoolSize: Int,
		keepAliveTimeInSeconds: Int = 60,
		workQueue: BlockingQueue[Runnable] = new SynchronousQueue
	) =
		new Executor with Shutdown
		{
			override protected val executorService = new ThreadPoolExecutor(
				corePoolSize,
				maximumPoolSize,
				keepAliveTimeInSeconds,
				TimeUnit.SECONDS,
				workQueue)
		}

	def newCachedThreadPoolCompletionService[V](
		corePoolSize: Int,
		maximumPoolSize: Int,
		keepAliveTimeInSeconds: Int = 60,
		workQueue: BlockingQueue[Runnable] = new SynchronousQueue
	) =
		new CompletionExecutor[V](
			new ThreadPoolExecutor(
				corePoolSize,
				maximumPoolSize,
				keepAliveTimeInSeconds,
				TimeUnit.SECONDS,
				workQueue)
		)

	def newScheduledThreadPool(corePoolSize: Int, errorLogger: Throwable => Unit) =
		new Executor with Shutdown with Scheduling
		{
			override protected val executorService = new ScheduledThreadPoolExecutor(corePoolSize)
			override val onError = errorLogger
		}

	def newScheduledThreadPool(corePoolSize: Int) =
		new Executor with Shutdown with Scheduling
		{
			override protected val executorService = new ScheduledThreadPoolExecutor(corePoolSize)
			override val onError = (t: Throwable) => t.printStackTrace
		}

	def newFixedThreadPool(nThreads: Int) =
		new Executor with Shutdown
		{
			override protected val executorService = Executors.newFixedThreadPool(nThreads)
		}

	def newFixedThreadPoolCompletionService[V](nThreads: Int) =
		new CompletionExecutor[V](Executors.newFixedThreadPool(nThreads))

	/**
	  * creates an executor of nThread, submits f() x times and returns V x times
	  * as returned by f(). It then shutsdown the executor.
	  *
	  * f: Int => V , where Int is the i-th execution, i is between [1..times]
	  * inclusive.
	  *
	  * If any of the invocation of f() fails, the executor will be shut down
	  * and no further threads will be submitted to it. The exception will propagate
	  * to the caller.
	  */
	def lifecycle[V](nThreads: Int, times: Int)(f: Int => V): Seq[V] = {
		val pool = newFixedThreadPool(nThreads)
		try {
			val seq = for (i <- 1 to times) yield pool.submit(f(i))
			seq.map(_.get)
		} finally {
			pool.shutdown
		}
	}

	/**
	  * creates an executor of nThread, submits f() x params.size and returns V x params.size
	  * as returned by f(). It then shutsdown the executor.
	  *
	  * f: T => V , each thread getting a different parameter from the traversable
	  *
	  * If any of the invocation of f() fails, the executor will be shut down
	  * and no further threads will be submitted to it. The exception will propagate
	  * to the caller.
	  */
	def lifecycle[T, V](nThreads: Int, params: Traversable[T])(f: T => V): Traversable[V] = {
		val pool = newFixedThreadPool(nThreads)
		try {
			val results = params.map(param => pool.submit(f(param)))
			results.map(_.get)
		} finally {
			pool.shutdown
		}
	}
}

/**
  * wrapper for the ExecutorService
  *
  * new Executor with Shutdown
  */
abstract class Executor
{
	// ideally the underlying executor should not be accessible
	protected val executorService: ExecutorService

	/**
	  * submits a task for execution and returns a Future.
	  *
	  * example:
	  * <code>
	  * val future=executor.submit {
	  * // will run on a separate thread soon
	  * 25
	  * }
	  * ...
	  * val result = future.get // result=25
	  * </code>
	  */
	def submit[R](f: => R) = executorService.submit(new Callable[R]
	{
		def call = f
	})

	def submit[V](task: Callable[V]) = executorService.submit(task)

	def submit(task: Runnable) = executorService.submit(task)
}

/*
 * new Executor with Scheduling with Shutdown
 */
trait Scheduling
{
	protected val executorService: ScheduledExecutorService
	val onError: Throwable => Unit

	/**
	  * schedules a task to run in the future
	  *
	  * example:
	  *
	  * <code>
	  * val future=schedule(100,TimeUnit.MILLISECONDS) {
	  * // to do in 100 millis from now
	  * }
	  * ...
	  * val result=future.get
	  * </code>
	  */
	def schedule[R](delay: Long, unit: TimeUnit)(f: => R): ScheduledFuture[R] = executorService.schedule(new Callable[R]
	{
		def call = f
	}, delay, unit)

	/**
	  * schedule a task to run in the future.
	  *
	  * example:
	  * <code>
	  * usage: val future=schedule(DateTime.now + 2.days) {
	  * // to do in 2 days from now
	  * }
	  * </code>
	  */
	def schedule[R](runAt: DateTime): (=> R) => ScheduledFuture[R] = {
		val dt = runAt.getMillis - System.currentTimeMillis
		if (dt < 0) throw new IllegalArgumentException("next run time is in the past : %s".format(runAt))
		schedule(dt, TimeUnit.MILLISECONDS) _
	}

	/**
	  * runs a task periodically. The task initially runs on firstTime. The result R is then
	  * used to call process(R) and if that returns a new DateTime, the task will be executed
	  * again on that time. If process(R) returns None, the task won't be executed again.
	  *
	  * This method returns straight away, any processing occurs on separate threads using
	  * the executor.
	  *
	  * If the task throws an exception, the onError function will be called to log
	  * the error (by default it prints the stacktrace to the console)
	  *
	  * If the process throws an exception, the scheduling of the task will stop.
	  *
	  * @param firstRun DateTime of the first run, i.e. DateTime.now + 2.seconds
	  * @param process  a function to process the result and specify the next
	  *                 time the task should run. The value is calculated after f is
	  *                 executed and if None the task will not be executed anymore.
	  * @param f        the task
	  */
	def runPeriodically[R](firstRun: DateTime, process: Option[R] => Option[DateTime])(f: => R): Unit =
		schedule(firstRun) {
			val r = try {
				Some(f)
			} catch {
				case e: Throwable =>
					onError(e)
					None
			}
			process(r) match {
				case Some(nextRun) => runPeriodically(nextRun, process)(f)
				case None =>
			}
		}

	/**
	  * periodically runs f, starting on firstRun and repeating according to
	  * the calculated "process" value.
	  *
	  * example:
	  * <code>
	  *
	  * import org.scala_tools.time.Imports._
	  *
	  * val executorService = ExecutorServiceManager.newScheduledThreadPool(5)
	  *
	  * val start = System.currentTimeMillis
	  * executorService.runPeriodically(DateTime.now + 50.millis, Some(DateTime.now + 1.second)) {
	  * // should print dt 6 times, once per second
	  * println("dt:%d".format(System.currentTimeMillis - start))
	  * }
	  *
	  * Thread.sleep(5500)
	  * executorService.shutdownAndAwaitTermination(DateTime.now + 100.millis)
	  *
	  * </code>
	  *
	  * If the task throws an exception, the onError function will be called to log
	  * the error (by default it prints the stacktrace to the console)
	  *
	  * If the process throws an exception, the scheduling of the task will stop.
	  *
	  * @param firstRun       DateTime of the first run, i.e. DateTime.now + 2.seconds
	  * @param whenToReRun    a by-value parameter specifying the next time the task should
	  *                       run. The value is calculated after f is executed and if None
	  *                       the task will not be executed anymore.
	  * @param f              the task
	  */
	def runPeriodically[R](firstRun: DateTime, whenToReRun: => Option[DateTime])(f: => R): Unit =
		schedule(firstRun) {
			try {
				f
			} catch {
				case e: Throwable => onError(e)
			}
			whenToReRun match {
				case Some(nextRun) => runPeriodically(nextRun, whenToReRun)(f)
				case None =>
			}
		}
}

/**
  * provides shutdown services to Executor
  */
trait Shutdown
{
	protected val executorService: ExecutorService

	def shutdown = executorService.shutdown

	def shutdownNow = executorService.shutdownNow

	def awaitTermination(timeout: Long, unit: TimeUnit): Unit = executorService.awaitTermination(timeout, unit)

	def awaitTermination(timeoutWhen: DateTime): Unit = awaitTermination(timeoutWhen.getMillis - System.currentTimeMillis, TimeUnit.MILLISECONDS)

	def shutdownAndAwaitTermination(waitTimeInSeconds: Int) {
		shutdown
		awaitTermination(waitTimeInSeconds, TimeUnit.SECONDS)
	}

	def shutdownAndAwaitTermination(timeoutWhen: DateTime) {
		shutdown
		awaitTermination(timeoutWhen)
	}
}

/*
 * @see CompletionService
 */
class CompletionExecutor[V](protected val executorService: ExecutorService) extends Shutdown
{
	private val completionService = new ExecutorCompletionService[V](executorService)

	def submit(f: => V): Future[V] = completionService.submit(new Callable[V]
	{
		def call = f
	})

	def submit(task: Callable[V]) = completionService.submit(task)

	def submit(task: Runnable, result: V) = completionService.submit(task, result)

	/**
	  * Retrieves and removes the Future representing the next
	  * completed task, waiting if none are yet present.
	  *
	  * @return the Future representing the next completed task
	  * @throws InterruptedException if interrupted while waiting
	  */
	def take: Future[V] = completionService.take

	/**
	  * Retrieves and removes the Future representing the next
	  * completed task or <tt>None</tt> if none are present.
	  *
	  * @return the Future representing the next completed task, or
	  *         <tt>None</tt> if none are present
	  */
	def poll: Option[Future[V]] = {
		val t = completionService.poll
		if (t == null) None else Some(t)
	}

	/**
	  * Retrieves and removes the Future representing the next
	  * completed task, waiting if necessary up to the specified wait
	  * time if none are yet present.
	  *
	  * @param timeout how long to wait before giving up, in units of
	  *                <tt>unit</tt>
	  * @param unit    a <tt>TimeUnit</tt> determining how to interpret the
	  *                <tt>timeout</tt> parameter
	  * @return the Future representing the next completed task or
	  *         <tt>None</tt> if the specified waiting time elapses
	  *         before one is present
	  * @throws InterruptedException if interrupted while waiting
	  */
	def poll(timeout: Long, unit: TimeUnit): Option[Future[V]] = {
		val t = completionService.poll(timeout, unit)
		if (t == null) None else Some(t)
	}

	/**
	  * polls, waiting max until the provided DateTime.
	  */
	def poll(till: DateTime): Option[Future[V]] = pollWaitInMillis(till.getMillis - System.currentTimeMillis)

	def pollWaitInMillis(timeoutMs: Long): Option[Future[V]] = poll(timeoutMs, TimeUnit.MILLISECONDS)
}
