package com.googlecode.scalascriptengine

import java.io.File

/**
 * this holds the configuration for the script engine. Source paths,
 * compilation class paths, classloading classpaths and output directory
 * for the compiled files. In the future it will hold scala compiler
 * settings, error reporters and so on.
 */
case class Config(
		val sourcePaths: Set[File], // this  is where the source files are located
		// this is the classpath for compilation and must be provided. i.e.
		// ScalaScriptEngine.currentClassPath
		val compilationClassPaths: Set[File] = ScalaScriptEngine.currentClassPath,
		// this is an extra class loading classpath. I.e. the script folder might
		// utilize extra jars. Also the parent classloader will be used
		// to find any unresolved classes. This means that all classes visible to
		// your application will also be visible to the scripts even if the 
		// classLoadingClassPaths is empty
		val classLoadingClassPaths: Set[File] = Set(),
		// the outputDir, this is where all compiled classes will be stored. Please
		// use with care! A folder in the temp directory will usually do.
		val outputDir: File = ScalaScriptEngine.tmpOutputFolder,
		classLoaderConfig: ClassLoaderConfig = ClassLoaderConfig.default) {

	// a convenient constructor to create a config with the default options
	// and one only source folder.
	def this(sourcePath: File) = this(Set(sourcePath))
}
