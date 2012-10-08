package test

import com.googlecode.scalascriptengine.TestClassTrait
import java.io.File

class TryHome extends TestClassTrait {
	def result = {
		val f=new File("/home")
		if(f.isDirectory) "directory" else "file"
	}
}
