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
object InsttantiatingWithConstructorArgs extends App {
	val sourceDir = new File("examplefiles/constructors")
	val sse = ScalaScriptEngine.onChangeRefresh(sourceDir)
	sse.deleteAllClassesInOutputDirectory
	sse.refresh
	val constructors = sse.constructors[UserTrait]("my.User")
	val user = constructors.newInstance("Kostas Kougios", 10)
	println(user) // prints User(Kostas Kougios,10)
}

trait UserTrait {
	val name: String
	val age: Int
}
