package com.googlecode.scalascriptengine.internals

import java.io.{File, FileInputStream}

/**
 * @author kostantinos.kougios
 *
 * 23 Dec 2011
 */
object Utils
{
	def toBytes(f: File) = {
		val fis = new FileInputStream(f)
		try {
			val l = f.length()
			val a = new Array[Byte](l.toInt)
			fis.read(a)
			a
		} finally {
			fis.close
		}
	}
}