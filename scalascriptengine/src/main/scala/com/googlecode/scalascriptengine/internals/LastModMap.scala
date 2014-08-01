package com.googlecode.scalascriptengine.internals

import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * @author kostas.kougios
 *         Date: 16/02/13
 */
class LastModMap
{
	private val modified = new ConcurrentHashMap[File, java.lang.Long]

	def isMod(f: File) = {
		val r = modified.get(f) match {
			case null => true
			case l => l < f.lastModified
		}
		r
	}

	def updated(f: File) {
		modified.put(f, f.lastModified)
	}

	def markAllAsModified() {
		modified.clear()
	}
}
