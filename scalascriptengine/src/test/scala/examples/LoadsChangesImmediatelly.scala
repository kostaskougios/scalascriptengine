package examples
import com.googlecode.scalascriptengine.ScalaScriptEngine
import java.io.File

/**
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