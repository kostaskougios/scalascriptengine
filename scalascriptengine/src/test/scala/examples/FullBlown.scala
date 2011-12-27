package examples
import com.googlecode.scalascriptengine.OnChangeRefresh
import com.googlecode.scalascriptengine.RefreshAsynchronously
import com.googlecode.scalascriptengine.ScalaScriptEngine
import java.io.File

/**
 * @author kostantinos.kougios
 *
 * 27 Dec 2011
 */
object FullBlown extends App {
	// the source directory
	val sourceDir = new File("examplefiles/simple")
	// compilation classpath
	val compilationClassPath = ScalaScriptEngine.currentClassPath
	// runtime classpath (empty). All other classes are loaded by the parent classloader
	val runtimeClasspath = Set[File]()
	// the output dir for compiled classes
	val outputDir = new File(System.getProperty("java.io.tmpdir"), "scala-script-engine-classes")
	outputDir.mkdir

	val sse = new ScalaScriptEngine(
		Set(sourceDir),
		compilationClassPath,
		runtimeClasspath,
		outputDir) with OnChangeRefresh with RefreshAsynchronously {
		val recheckEveryMillis: Long = 1000 // each file will only be checked once per second
	}

	sse.refresh
	while (true) {
		val t = sse.newInstance[TryMeTrait]("my.TryMe")
		println("code version %d, result : %s".format(sse.versionNumber, t.result))
		Thread.sleep(500)
	}
}