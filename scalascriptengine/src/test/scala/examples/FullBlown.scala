package examples

import java.io.File
import com.googlecode.scalascriptengine._
import com.googlecode.scalascriptengine.Config

/**
 * This example shows how to instantiate the script engine without using the factory
 * methods. This allows for full customization as there are several traits that can
 * be mixed in to provide additional functionality.
 *
 * The traits mixed in are RefreshAsynchronously and FromClasspathFirst. RefreshAsynchronously
 * will do the compilation of changed files in the background and when ready will use the
 * new compiled classes. FromClasspathFirst will load the class from the classpath if it exists
 * and if not it will compile it. This is handy during dev if i.e. your IDE already compiled
 * the scala classes and those are in the classpath.
 *
 * This example will run continiously, allowing the user to change one of the used
 * scala classes.
 *
 *
 * @author kostantinos.kougios
 *
 *         27 Dec 2011
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

	val sse = new ScalaScriptEngine(Config(
		List(SourcePath(sourceDir, outputDir)),
		compilationClassPath,
		runtimeClasspath
	)) with RefreshAsynchronously with FromClasspathFirst {
		val recheckEveryMillis: Long = 1000 // each file will only be checked maximum once per second
	}

	// delete all compiled classes (i.e. from previous runs)
	sse.deleteAllClassesInOutputDirectory
	// since the refresh occurs async, we need to do the 1st refresh otherwise initially my.TryMe
	// class will not be found
	sse.refresh

	while (true) {
		val t = sse.newInstance[TryMeTrait]("my.TryMe")
		println("code version %d, result : %s".format(sse.versionNumber, t.result))
		Thread.sleep(500)
	}
}