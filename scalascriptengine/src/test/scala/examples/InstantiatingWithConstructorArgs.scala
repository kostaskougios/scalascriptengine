package examples
import com.googlecode.scalascriptengine.ScalaScriptEngine
import java.io.File

/**
 * This example instantiates a dynamically loaded class with
 * a 2 parameter constructor.
 *
 * @author kostantinos.kougios
 *
 * 5 Jan 2012
 */
object InstantiatingWithConstructorArgs extends App {
	val sourceDir = new File("examplefiles/constructors")
	val sse = ScalaScriptEngine.onChangeRefresh(sourceDir)
	sse.deleteAllClassesInOutputDirectory
	sse.refresh
	// get constructors for the current codeversion.
	// please note that in order to always get a new instance
	// of the latest version of the class you will need
	// to always get the constructor from sse before using
	// it to instantiate a class

	val constructors = sse.constructors[UserTrait]("my.User")
	val user = constructors.newInstance("Kostas Kougios", 10)
	println(user) // prints User(Kostas Kougios,10)

	// now assuming we want to construct multiple instances of
	// my.User, we can get a function that will create instances
	// every time it is called. This constructor always uses
	// the current codeversion and if my.User changes then
	// the change will *not* be reflected to the constructor.
	val constructor = constructors.constructorWith2Args[String, Int]
	println(constructor("Kostas", 5)) // prints User(Kostas,5)
	println(constructor("Joe", 15)) // prints User(Joe,15)
}

trait UserTrait {
	val name: String
	val age: Int
}
