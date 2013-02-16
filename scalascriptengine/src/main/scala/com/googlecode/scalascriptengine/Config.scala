package com.googlecode.scalascriptengine

import java.io.File

/**
 * this holds the configuration for the script engine. Source and output paths,
 * compilation class paths, classloading classpaths . In the future it will hold scala compiler
 * settings, error reporters and so on.
 */
case class Config(
	                 // this  is where the source files and target class directories are located
	                 // each source folder is compiled and in turn is used as classpath for the next source folder.
	                 // hence source folders must be in order of dependency, with the root classes been the
	                 // first element of the list
	                 val sourcePaths: List[SourcePath],
	                 // this is the classpath for compilation and must be provided. i.e.
	                 // ScalaScriptEngine.currentClassPath
	                 val compilationClassPaths: Set[File] = ScalaScriptEngine.currentClassPath,
	                 // this is an extra class loading classpath. I.e. the script folder might
	                 // utilize extra jars. Also the parent classloader will be used
	                 // to find any unresolved classes. This means that all classes visible to
	                 // your application will also be visible to the scripts even if the
	                 // classLoadingClassPaths is empty
	                 val classLoadingClassPaths: Set[File] = Set(),

	                 classLoaderConfig: ClassLoaderConfig = ClassLoaderConfig.default) {

	// a convenient constructor to create a config with the default options
	// and one only source folder.
	def this(sourcePath: File) = this(List(SourcePath(sourcePath)))

	val scalaSourceDirs = sourcePaths.map(_.sourceDir)
	val targetDirs = sourcePaths.map(_.targetDir)
}

/**
 * scala source folder along with the destination class folder
 *
 * @param sourceDir     root folder of scala sources
 * @param targetDir     root folder of generated class files
 */
case class SourcePath(
	                     sourceDir: File,
	                     // the outputDir, this is where all compiled classes will be stored. Please
	                     // use with care! A folder in the temp directory will usually do.
	                     targetDir: File = ScalaScriptEngine.tmpOutputFolder
	                     ) {
	val sourceDirPath = sourceDir.getAbsolutePath

	def isModified(clz: String) = {
		val f = clz.replace('.', '/')
		val scalaName = f + ".scala"
		val clzName = f + ".class"

		val clzFile = new File(targetDir, clzName)
		if (!clzFile.exists)
			true
		else {
			val scalaFile = new File(sourceDir, scalaName)
			scalaFile.lastModified > clzFile.lastModified
		}
	}

	def allChanged: Set[File] = {

		def scan(srcDir: File, clzDir: File): Set[File] = {
			val all = srcDir.listFiles
			val mod = all.filter(_.getName.endsWith(".scala"))
				.filter {
				scalaFile =>
					val clzName = scalaFile.getName.substring(0, scalaFile.getName.length - 5) + "class"
					val clzFile = new File(clzDir, clzName)
					if (!clzFile.exists)
						true
					else {
						scalaFile.lastModified > clzFile.lastModified
					}
			}.toSet

			val sub = all.filter(_.isDirectory).map {
				dir =>
					scan(dir, new File(clzDir, dir.getName))
			}.flatten

			mod ++ sub
		}

		scan(sourceDir, targetDir)
	}
}