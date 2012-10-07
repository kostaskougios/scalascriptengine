package reload

import com.googlecode.scalascriptengine.TestClassTrait
import scala.io.Source
class Main extends TestClassTrait
{
	def result= {
		val in=getClass.getResourceAsStream("version.txt")
		val src=Source.fromInputStream(in)
		src.getLines().next
	}
} 