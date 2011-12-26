package my

import examples.TryMeTrait

class TryMe extends TryMeTrait {
	val  r="%d : change me while example runs!"
		
	override def result={
		import TryMe._
		counter+=1
		r.format(counter)
	}
}

object TryMe
{
	var counter=0
}