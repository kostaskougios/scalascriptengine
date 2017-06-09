package test

import java.io.File

class TryHome extends com.googlecode.scalascriptengine.TestClassTrait
{
	def result = {
		val f = new File("/home")
		if (f.isDirectory) "directory" else "file"
	}
}
