package com.googlecode.scalascriptengine

import java.io.File

import org.apache.commons.io.FileUtils

/**
 * @author kostantinos.kougios
 *
 *         22 Dec 2011
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
		if (dir.isDirectory) deleteDir(dir)
		dir.mkdirs
		dir
	}

	def cleanDestinationAndCopyFromSource(src: File, dest: File) {
		deleteDir(dest)
		copyFromSource(src, dest)
	}

	var time = System.currentTimeMillis - 50000

	def copyFromSource(src: File, dest: File) = {

		def replaceTime(dir: File) {
			val files = dir.listFiles
			files.filter(_.isDirectory).foreach(d => replaceTime(d))
			files.filter(!_.isDirectory).foreach(_.setLastModified(time))
		}

		FileUtils.copyDirectory(src, dest, false)
		replaceTime(dest)
		time += 2000
	}

	def makeDummyFile(dir: File, name: String, time: Option[Long] = None) = {
		val f = new File(dir, name)
		FileUtils.touch(f)
		time.foreach {
			t =>
				f.setLastModified(t)
		}
		f
	}
}