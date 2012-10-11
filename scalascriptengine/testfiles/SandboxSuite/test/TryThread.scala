package test

import com.googlecode.scalascriptengine.TestClassTrait

class TryThread extends TestClassTrait {
	def result = {
		new Thread {
			override def run = {
				println("TryThread>thread>hacked!")
			}
		}.start()
		Thread.sleep(100)
		"TryThread>hacked!"
	}
}
