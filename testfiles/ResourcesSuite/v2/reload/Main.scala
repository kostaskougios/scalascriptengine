package reload

import scala.io.Source

class Main extends com.googlecode.scalascriptengine.TestClassTrait
{
	def result = {
		val in = getClass.getResourceAsStream("version.txt")
		val src = Source.fromInputStream(in)
		src.getLines().next
	}
} 