package com.googlecode.scalascriptengine.internals

import java.util.concurrent.ConcurrentHashMap
import java.io.File

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
}
