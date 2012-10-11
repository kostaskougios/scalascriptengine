package test

import com.googlecode.scalascriptengine.TestClassTrait

class TryThread extends TestClassTrait {
	def result = {
		new Thread {
			override def run = {
				println("hacked!")
			}
		}.start()
		Thread.sleep(100)
		"hacked!"
	}
}
