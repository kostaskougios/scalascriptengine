package examples
import com.googlecode.scalascriptengine.ScalaScriptEngine
import java.io.File

/**
 * This example dynamically compiles and reloads changes from the sourceDir.
 * When a change is detected, the script engine compiles the source files (blocking).
 * When compilation is complete, the new version of the class is returned.
 * If there is a compilation error, the script engine returns the previous
 * valid version of my.TryMe
 *
 * This example demonstrates the on-change-refresh policy of the script engine.
 *
 * @author kostantinos.kougios
 *
 * 26 Dec 2011
 */
object LoadsChangesImmediatelly extends App {

	val sourceDir = new File("examplefiles/simple")
	val sse = ScalaScriptEngine.onChangeRefresh(sourceDir)

	while (true) {
		val t = sse.newInstance[TryMeTrait]("my.TryMe")
		println("code version %d, result : %s".format(sse.versionNumber, t.result))
		Thread.sleep(1000)
	}
}