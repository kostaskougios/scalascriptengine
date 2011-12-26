package examples
import com.googlecode.scalascriptengine.ScalaScriptEngine
import java.io.File

/**
 * This example dynamically compiles and reloads changes from the sourceDir.
 * When a change is detected, the script engine compiles the source files (non-blocking).
 * When compilation is complete, the new version of the class is returned but till then
 * any calls to get the class will return the previous version of my.TryMe.
 * If there is a compilation error, the script engine returns the previous
 * valid version of my.TryMe
 *
 * This example demonstrates the on-change-refresh-async policy of the script engine.
 *
 * @author kostantinos.kougios
 *
 * 26 Dec 2011
 */
object CompileChangesButLoadThemWhenReady extends App {
	val sourceDir = new File("examplefiles/simple")
	val sse = ScalaScriptEngine.onChangeRefreshAsynchronously(sourceDir)
	// because code changes are loaded async, we need to do a compilation
	// before we request a class for the first time otherwise exceptions
	// will be thrown till the 1st code version is compiled.
	try {
		sse.refresh

		while (true) {
			val t = sse.newInstance[TryMeTrait]("my.TryMe")
			println("code version %d, result : %s".format(sse.versionNumber, t.result))
			Thread.sleep(500)
		}
	} finally {
		// since the async policy uses a background thread, we need to shut
		// it down when done.
		sse.shutdown
	}
}