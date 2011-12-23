package com.googlecode.scalascriptengine

import java.io.File
import org.apache.commons.io.FileUtils
import java.util.UUID

/**
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
package object scalascriptengine {
	def tmpDirStr = System.getProperty("java.io.tmpdir")
	def tmpDir = new File(tmpDirStr)
	def deleteDir(dir: File) = {
		if (!dir.getAbsolutePath.startsWith(tmpDirStr)) throw new IllegalStateException("only deleting from tmp folder allowed in order to avoid damage")
		FileUtils.deleteDirectory(dir)
	}
	def newTmpDir(name: String) = {
		val dir = new File(tmpDir, name)
		dir.mkdir
		dir
	}
	def cleanDestinationAndCopyFromSource(src: File, dest: File) {
		deleteDir(dest)
		FileUtils.copyDirectoryToDirectory(src, dest)
	}
}