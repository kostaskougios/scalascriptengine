package test

import java.io.File

class TryFile extends com.googlecode.scalascriptengine.TestClassTrait
{
	def result = {
		val a = A("hi")
		println(a.name)
		val f = new File("/tmp")
		if (f.isDirectory) "directory" else "file"
	}
}
