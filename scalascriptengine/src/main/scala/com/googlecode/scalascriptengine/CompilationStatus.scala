package com.googlecode.scalascriptengine

import org.scala_tools.time.Imports._
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author kostantinos.kougios
 *
 * 11 Jan 2012
 */
class CompilationStatus private (val startTime: DateTime, val stopTime: Option[DateTime], val step: CompilationStatus.Status) {
	import CompilationStatus._
	private val stopTrigger = new AtomicBoolean(false)
	def stop: Unit = step match {
		case ScanningSources | Compiling => stopTrigger.set(true)
	}
	def stopIfCompiling: Unit = stopTrigger.set(true)
}

object CompilationStatus {
	abstract class Status
	object NotYetReady extends Status
	object ScanningSources extends Status
	object Compiling extends Status
	object Complete extends Status

	def notYetReady = new CompilationStatus(DateTime.now, None, NotYetReady)
}