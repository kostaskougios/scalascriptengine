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

	val sourcePath = new SourcePath(srcDir, targetDir)

	before {
		deleteDir(srcDir)
		deleteDir(targetDir)
		srcDir.mkdir()
		targetDir.mkdir()
	}

	test("isModified, uncompiled file, positive") {
		makeDummyFile(srcDir, "A.scala")
		sourcePath.isModified("A") should be(true)
	}

	test("isModified, compiled file, negative") {
		makeDummyFile(srcDir, "A.scala")
		makeDummyFile(targetDir, "A.class")
		sourcePath.isModified("A") should be(false)
	}

	test("isModified, in package, positive") {
		makeDummyFile(new File(targetDir, "package"), "A.class", Some(10000))
		makeDummyFile(new File(srcDir, "package"), "A.scala", Some(20000))
		sourcePath.isModified("package.A") should be(true)
	}

	test("isModified, in package, negative") {
		makeDummyFile(new File(srcDir, "package"), "A.scala")
		makeDummyFile(new File(targetDir, "package"), "A.class")
		sourcePath.isModified("package.A") should be(false)
	}

	test("allChanged, uncompiled file, positive") {
		val src1 = makeDummyFile(new File(srcDir, "package"), "A.scala")
		val src2 = makeDummyFile(new File(srcDir, "package"), "B.scala")
		sourcePath.allChanged should be(Set(src1, src2))
	}

	test("allChanged, compiled file, positive") {
		makeDummyFile(new File(targetDir, "package"), "A.class", Some(10000))
		makeDummyFile(new File(targetDir, "package"), "B.class", Some(10000))
		val src1 = makeDummyFile(new File(srcDir, "package"), "A.scala", Some(20000))
		val src2 = makeDummyFile(new File(srcDir, "package"), "B.scala", Some(20000))

		sourcePath.allChanged should be(Set(src1, src2))
	}
}