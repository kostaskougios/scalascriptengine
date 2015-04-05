package examples

import java.io.File

import com.googlecode.scalascriptengine.ScalaScriptEngine
import org.joda.time.DateTime

/**
 * demonstrates the timed refresh policy. A background thread scans the
 * source folders for changes every second. If a change is detected,
 * it re-compiles the source folders and when done, the new code
 * version is used.
 *
 * @author kostantinos.kougios
 *
 *         27 Dec 2011
 */
object TimedRefresh extends App
{
	val sourceDir = new File("examplefiles/simple")
	val sse = ScalaScriptEngine.timedRefresh(sourceDir, () => DateTime.now.plusSeconds(1))
	sse.refresh
	while (true) {
		val t = sse.newInstance[TryMeTrait]("my.TryMe")
		println("code version %d, result : %s".format(sse.versionNumber, t.result))
		Thread.sleep(500)
	}
}