package com.googlecode.scalascriptengine

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scalascriptengine._
import java.io.File

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class SourcePathSuite extends FunSuite with ShouldMatchers with BeforeAndAfter {
	val srcDir = newTmpDir("source-path-suite-src")
	val targetDir = newTmpDir("source-path-suite-target")

	before {
		deleteDir(srcDir)
		deleteDir(targetDir)
		srcDir.mkdir()
		targetDir.mkdir()
	}

	test("isModified, new file, positive") {
		val sourcePath = new SourcePath(srcDir, targetDir)
		makeDummyFile(srcDir, "A.scala")
		sourcePath.isModified("A") should be(true)
	}

	test("isModified, in package, positive") {
		val sourcePath = new SourcePath(srcDir, targetDir)
		makeDummyFile(new File(targetDir, "package"), "A.class", Some(10000))
		sourcePath.isModified("package.A") should be(true)
	}

	test("allChanged, new files, positive") {
		val sourcePath = new SourcePath(srcDir, targetDir)
		val src1 = makeDummyFile(new File(srcDir, "package"), "A.scala")
		val src2 = makeDummyFile(new File(srcDir, "package"), "B.scala")
		sourcePath.allChanged should be(Set(src1, src2))
	}
}