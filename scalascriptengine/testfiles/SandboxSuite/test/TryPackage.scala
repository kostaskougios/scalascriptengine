package test

import com.googlecode.scalascriptengine.TestClassTrait

class TryPackage extends TestClassTrait {
	def result = {
		classOf[javax.swing.Icon].getName
	}
}
