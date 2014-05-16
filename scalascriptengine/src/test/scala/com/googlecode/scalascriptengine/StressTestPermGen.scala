package com.googlecode.scalascriptengine

/**
 * checks if we leak into perm-gen
 *
 * @author	kostas.kougios
 *            Date: 16/05/14
 */
object StressTestPermGen extends App
{
	(1 to 1000000).par.foreach {
		i =>
			val ect = EvalCode.with1Arg[String, Int]("s", s"s.toInt+${i}")

			// Now create a new instance of this function
			val x = ect.newInstance
			x("15")
	}
}
