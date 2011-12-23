package com.googlecode.scalascriptengine
import java.io.File
import java.io.FileInputStream

/**
 * @author kostantinos.kougios
 *
 * 23 Dec 2011
 */
protected object Utils {
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