package test

import com.googlecode.scalascriptengine.TestClassTrait
import java.io.File

class TryFile extends TestClassTrait {
	def result = {
		val f=new File("/tmp")
		if(f.isDirectory) "directory" else "file"
	}
}
