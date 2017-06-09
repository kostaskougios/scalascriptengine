package test

class TryThread extends com.googlecode.scalascriptengine.TestClassTrait
{
	def result = {
		new Thread
		{
			override def run = {
				println("TryThread>thread>hacked!")
			}
		}.start()
		Thread.sleep(100)
		"TryThread>hacked!"
	}
}
