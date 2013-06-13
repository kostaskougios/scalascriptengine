package com.googlecode.scalascriptengine

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import java.io.File
import scalascriptengine._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.concurrent.ExecutorServiceManager
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean
import java.io.FileWriter
import java.util.concurrent.atomic.AtomicLong

/**
 * @author kostantinos.kougios
 *
 *         27 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class EndToEndSuite extends FunSuite with ShouldMatchers
{
	val sourceDir = new File("testfiles/EndToEndSuite")

	test("multi-threaded") {

		def write(f: File, version: Int) {
			val fw = new FileWriter(f)
			try {
				fw.write( """
package reload
import com.googlecode.scalascriptengine.TestClassTrait

class Main extends TestClassTrait
{
	override def result="v%d"
}
				          """.format(version))
			} finally {
				fw.close
			}
		}
		val destDir = newTmpDir("dynamicsrc/reload")
		val main = new File(destDir, "Main.scala")
		write(main, 1)
		val sse = ScalaScriptEngine.onChangeRefreshAsynchronously(destDir)
		sse.deleteAllClassesInOutputDirectory
		sse.refresh

		val errors = new AtomicInteger
		val done = new AtomicBoolean(false)
		val iterations = new AtomicLong
		val executor = ExecutorServiceManager.newSingleThreadExecutor
		try {
			executor.submit {
				ExecutorServiceManager.lifecycle(40, 40) {
					_ =>
						while (!done.get) {
							try {
								val codeVersion = sse.currentVersion
								val t = codeVersion.newInstance[TestClassTrait]("reload.Main")
								if (t.result != "v" + codeVersion.version) {
									errors.incrementAndGet
									println(t.result)
								}

								// just to trigger a reload
								sse.newInstance[TestClassTrait]("reload.Main")
								Thread.sleep(1)
								iterations.incrementAndGet
							} catch {
								case e: Throwable =>
									errors.incrementAndGet
									e.printStackTrace
							}
						}
				}
				println("executor finished")
			}
			var currentVersion = 0
			for (i <- 1 to 10) {
				if (currentVersion != sse.versionNumber) {
					println("version is %d , iterations so far : %d".format(sse.versionNumber, iterations.get))
					currentVersion = sse.versionNumber
					Thread.sleep(1100) // make sure filesystem marks  the file as modified
					write(main, currentVersion + 1)
				}
				Thread.sleep(500)
				errors.get should be === 0
			}
			done.set(true)
			errors.get should be === 0
		} finally {
			executor.shutdownAndAwaitTermination(1)
			sse.shutdown
		}
	}
}