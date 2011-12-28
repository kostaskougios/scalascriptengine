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
import org.apache.commons.io.FileUtils
import scala.io.Source
import org.apache.commons.io.IOUtils
import java.io.FileWriter
/**
 * @author kostantinos.kougios
 *
 * 27 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class EndToEndSuite extends FunSuite with ShouldMatchers {
	val sourceDir = new File("testfiles/EndToEndSuite")

	test("multi-threaded") {

		def write(f: File, version: Int) {
			val fw = new FileWriter(f)
			try {
				fw.write(""" 
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
		write(main, 0)
		val sse = ScalaScriptEngine.onChangeRefreshAsynchronously(destDir)
		sse.deleteAllClassesInOutputDirectory
		sse.refresh

		val errors = new AtomicInteger
		val done = new AtomicBoolean(false)
		val executor = ExecutorServiceManager.newSingleThreadExecutor
		try {
			executor.submit {
				ExecutorServiceManager.lifecycle(40, 40) { _ =>
					while (!done.get) {
						val t = sse.newInstance[TestClassTrait]("Main")
						if (t.result != "v" + sse.versionNumber) errors.incrementAndGet
					}
				}
			}
			for (i <- 1 to 1000) {
				write(main, i)
				Thread.sleep(500)
				errors.get should be === 0
				println(sse.versionNumber)
			}
			done.set(true)
			errors.get should be === 0
		} finally {
			executor.shutdownAndAwaitTermination(1)
			sse.shutdown
		}
	}
}