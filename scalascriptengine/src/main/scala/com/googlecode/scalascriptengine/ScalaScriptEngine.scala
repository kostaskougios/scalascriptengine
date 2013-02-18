package com.googlecode.scalascriptengine

import java.io.File
import org.scala_tools.time.Imports._
import java.net.URLClassLoader

/**
 * The implementation of the script engine.
 *
 * The engine works by refreshing the codeVersion. This means that, when the
 * refresh() function is called and provided that the source files have changed,
 * a compilation will be triggered. When the compilation is complete, a new
 * codeVersion will be created and will be used till the next refresh.
 *
 * This can be initialized standalone or by mixing in a refresh policy trait. If
 * standalone, then refresh() should be manually invoked every time a change
 * occurs in the source code.
 *
 * If mixed in with a refresh policy, then the policy takes care of scanning the
 * source code for changes and refreshing. Please check RefreshPolicies.scala
 *
 * Typically this class will not be instantiated using 'new' but rather using one
 * of the factory methods of the companion object. Instantiation offers the full
 * amount of options that can be used by mixing in the various refresh policies
 * and enhancers.
 *
 * val sse = new ScalaScriptEngine(Config(
 * Set(sourceDir),
 * compilationClassPath,
 * runtimeClasspath,
 * outputDir)) with RefreshAsynchronously with FromClasspathFirst {
 * val recheckEveryMillis: Long = 1000 // each file will only be checked maximum once per second
 * }))
 *
 *
 * @author kostantinos.kougios
 *
 *         22 Dec 2011
 */
class ScalaScriptEngine(val config: Config) extends Logging {

	private def compileManager = new CompilerManager(config.sourcePaths, config.compilationClassPaths, this)

	// codeversion is initialy to version 0 which is not usable. 
	@volatile private var codeVersion: CodeVersion = new CodeVersion {
		override def version: Int = 0

		override def classLoader: ScalaClassLoader = throw new IllegalStateException("CodeVersion not yet ready.")

		override def files: List[SourceFile] = Nil

		override def sourceFiles = Map[File, SourceFile]()

		override def get[T](className: String): Class[T] = throw new IllegalStateException("CodeVersion not yet ready.")

		override def newInstance[T](className: String): T = throw new IllegalStateException("CodeVersion not yet ready.")

		override def constructors[T](className: String) = throw new IllegalStateException("CodeVersion not yet ready.")
	}

	@volatile private var _compilationStatus = CompilationStatus.notYetReady

	def currentVersion = codeVersion

	def versionNumber = codeVersion.version

	def compilationStatus = _compilationStatus

	/*
	 * refreshes the codeversion by scanning the source folders for changed source files. If any are
	 * found, then a compilation is triggered.
	 * 
	 * This method is not thread safe (but refresh policies ensure calling this only once at each time)
	 */
	def refresh: CodeVersion = {

		_compilationStatus = CompilationStatus.started

		val allChangedFiles = config.sourcePaths.map(paths => allChanged(paths)).flatten
		_compilationStatus.checkStop
		val result = if (allChangedFiles.isEmpty)
			codeVersion
		else {
			debug("refreshing changed files %s".format(allChangedFiles))
			val sourceFilesSet = allChangedFiles.map(f => SourceFile(f))
			_compilationStatus.checkStop

			def sourceFiles = sourceFilesSet.map(s => (s.file, s)).toMap

			try {
				_compilationStatus.checkStop
				compileManager.compile(allChangedFiles.map(_.getAbsolutePath))
			} catch {
				case e: Throwable =>
					if (versionNumber > 0) {
						// update fileset to this codeversion to avoid
						// continuously compiling problematic code
						codeVersion = CodeVersionImpl(
							codeVersion.version,
							sourceFilesSet,
							codeVersion.classLoader,
							sourceFiles)
					}
					_compilationStatus = CompilationStatus.failed(_compilationStatus)
					throw e
			}
			_compilationStatus.checkStop
			val classLoader = createClassLoader
			debug("done refreshing")
			codeVersion = CodeVersionImpl(
				codeVersion.version + 1,
				sourceFilesSet,
				classLoader,
				sourceFiles)
			codeVersion
		}

		_compilationStatus = CompilationStatus.completed(_compilationStatus)
		result
	}

	protected def createClassLoader = new ScalaClassLoader(
		config.sourcePaths.map(_.targetDir).toSet,
		config.scalaSourceDirs.toSet ++ config.classLoadingClassPaths,
		Thread.currentThread.getContextClassLoader,
		config.classLoaderConfig)

	/**
	 * returns the Class[T] for className
	 *
	 * Can throw ClassNotFoundException if the class is not present.
	 * Can throw ClassCastException if the class is not of T
	 * Can trigger a compilation in the background or foreground,
	 * depending on the refresh policy.
	 */
	def get[T](className: String): Class[T] = codeVersion.get(className)

	/**
	 * returns Constructors, this allows easy instantiation of the class
	 * using up to 4 constructor arguments.
	 *
	 * Constructors returned by this method are linked to the current codeversion.
	 * This means that, if codeversion is refreshed, a call to this will return
	 * an up to date Constructors instance. But also it means that the returned
	 * constructor will always create instances of that codeversion and will not
	 * reflect updates to the codeversion.
	 */
	def constructors[T](className: String): Constructors[T] = new Constructors(get(className))

	/**
	 * returns a new instance of className. The new instance is always of the
	 * latest codeversion.
	 */
	def newInstance[T](className: String): T = get[T](className).newInstance

	/**
	 * please make sure outputDir is valid!!! If you used one of the factory
	 * methods to create an instance of the script engine, the output dir will
	 * be in the tmp directory.
	 */
	def deleteAllClassesInOutputDirectory() {
		def deleteAllClassesInOutputDirectory(dir: File) {
			dir.listFiles.filter(_.getName.endsWith(".class")).foreach(_.delete)
			dir.listFiles.filter(_.isDirectory).foreach(d => deleteAllClassesInOutputDirectory(d))
		}
		config.targetDirs.foreach {
			d =>
				deleteAllClassesInOutputDirectory(d)
		}
	}

	private val modified = new LastModMap

	/**
	 * @param clz       the full class name
	 * @return          true if the scala file was modified since the last compilation
	 */
	def isModified(sourcePath: SourcePath, clz: String): Boolean = {
		val f = clz.replace('.', '/')
		val scalaName = f + ".scala"
		val scalaFile = new File(sourcePath.sourceDir, scalaName)
		modified.isMod(scalaFile)
	}

	private def allChanged(sourcePath: SourcePath): Set[File] = {

		def scan(srcDir: File, clzDir: File): Set[File] = {
			val all = srcDir.listFiles
			val mod = all.filter(_.getName.endsWith(".scala"))
				.filter {
				scalaFile =>
					modified.isMod(scalaFile)
			}.toSet

			val sub = all.filter(_.isDirectory).map {
				dir =>
					scan(dir, new File(clzDir, dir.getName))
			}.flatten

			mod ++ sub
		}

		val all = scan(sourcePath.sourceDir, sourcePath.targetDir)
		all.foreach(modified.updated(_))
		all
	}
}

/**
 * the companion object provides a lot of useful factory methods to create a script engine
 * with sensible defaults.
 */
object ScalaScriptEngine {
	def tmpOutputFolder = {
		val dir = new File(System.getProperty("java.io.tmpdir"), "scala-script-engine-classes")
		dir.mkdir
		dir
	}

	def currentClassPath = {
		// this tries to detect the classpath, if it doesn't work
		// for you, please email me or open an issue explaining your
		// usecase.
		def cp(cl: ClassLoader): Set[File] = cl match {
			case ucl: URLClassLoader => ucl.getURLs.map(u => new File(u.getFile)).toSet ++ cp(ucl.getParent)
			case _: ClassLoader => Set()
			case null => Set()
		}
		cp(Thread.currentThread.getContextClassLoader) ++ System.getProperty("java.class.path").split(File.pathSeparator).map(p => new File(p)).toSet
	}

	def defaultConfig(sourcePath: File) = Config(
		List(SourcePath(sourcePath)),
		currentClassPath,
		Set()
	)

	/**
	 * returns an instance of the engine. Refreshes must be done manually
	 */
	def withoutRefreshPolicy(
		                        sourcePaths: List[SourcePath],
		                        compilationClassPaths: Set[File],
		                        classLoadingClassPaths: Set[File]
		                        ): ScalaScriptEngine =
		new ScalaScriptEngine(Config(sourcePaths, compilationClassPaths, classLoadingClassPaths))

	/**
	 * returns an instance of the engine. Refreshes must be done manually
	 */
	def withoutRefreshPolicy(sourcePaths: List[SourcePath], compilationClassPaths: Set[File]): ScalaScriptEngine =
		new ScalaScriptEngine(Config(sourcePaths, compilationClassPaths, Set()))

	/**
	 * returns an instance of the engine. Refreshes must be done manually
	 */
	def withoutRefreshPolicy(sourcePath: SourcePath, compilationClassPaths: Set[File]): ScalaScriptEngine =
		withoutRefreshPolicy(Config(List(sourcePath), compilationClassPaths, Set()), compilationClassPaths)

	def withoutRefreshPolicy(config: Config, compilationClassPaths: Set[File]): ScalaScriptEngine =
		new ScalaScriptEngine(config)

	/**
	 * returns an instance of the engine. Refreshes must be done manually
	 */
	def withoutRefreshPolicy(sourcePaths: List[SourcePath]): ScalaScriptEngine =
		withoutRefreshPolicy(sourcePaths, currentClassPath)

	/**
	 * returns an instance of the engine. Refreshes must be done manually
	 */
	def withoutRefreshPolicy(sourcePath: SourcePath): ScalaScriptEngine = withoutRefreshPolicy(List(sourcePath))

	/**
	 * periodically scans the source folders for changes. If a change is detected, a recompilation is
	 * triggered. The new codeversion is used upon competion of the compilation.
	 *
	 * Please call refresh before using the engine for the first time.
	 */
	def timedRefresh(sourcePath: File, refreshEvery: () => DateTime): ScalaScriptEngine with TimedRefresh =
		timedRefresh(defaultConfig(sourcePath), refreshEvery)

	def timedRefresh(config: Config, refreshEvery: () => DateTime): ScalaScriptEngine with TimedRefresh =
		new ScalaScriptEngine(config) with TimedRefresh {
			def rescheduleAt = refreshEvery()
		}

	/**
	 * creates a ScalaScriptEngine with the following behaviour:
	 *
	 * if a change in a requested class source file is detected, the source path
	 * will be recompiled (this includes all changed files that changed). For each
	 * call to ScalaScriptEngine.get(className), the filesystem is checked for
	 * modifications in the relevant scala file. For a more efficient way of
	 * doing the same in production environments, please look at
	 * #onChangeRefresh(sourcePath, recheckEveryInMillis)
	 *
	 * @param sourcePath	the path where the scala source files are.
	 * @return				the ScalaScriptEngine
	 */
	def onChangeRefresh(sourcePath: File): ScalaScriptEngine with OnChangeRefresh =
		onChangeRefresh(sourcePath, 0)

	/**
	 * creates a ScalaScriptEngine with the following behaviour:
	 *
	 * if a change in a requested class source file is detected, the source path
	 * will be recompiled (this includes all changed files that changed). For each
	 * call to ScalaScriptEngine.get(className), provided that recheckEveryInMillis
	 * has passed between the call and the previous filesystem check,
	 * the filesystem is checked for
	 * modifications in the relevant scala file.
	 *
	 * @param sourcePath					the path where the scala source files are.
	 * @param recheckSourceEveryDtInMillis	each file will only be checked for changes
	 *                                        once per recheckEveryInMillis milliseconds.
	 * @return								the ScalaScriptEngine
	 */
	def onChangeRefresh(sourcePath: File, recheckSourceEveryDtInMillis: Long): ScalaScriptEngine with OnChangeRefresh =
		onChangeRefresh(defaultConfig(sourcePath), recheckSourceEveryDtInMillis)

	def onChangeRefresh(config: Config, recheckSourceEveryDtInMillis: Long): ScalaScriptEngine with OnChangeRefresh =
		new ScalaScriptEngine(config) with OnChangeRefresh with RefreshSynchronously {
			val recheckEveryMillis = recheckSourceEveryDtInMillis
		}

	/**
	 * similar to onChangeRefresh, but the compilation occurs in the background.
	 * While the compilation occurs, the ScalaScriptEngine.get(className)
	 * returns the existing version of the class without blocking.
	 *
	 * Please call refresh before using the engine for the first time.
	 *
	 * Before exiting, please call shutdown to shutdown the compilation thread
	 */
	def onChangeRefreshAsynchronously(sourcePath: File): ScalaScriptEngine with OnChangeRefresh with RefreshAsynchronously =
		onChangeRefreshAsynchronously(sourcePath, 0)

	/**
	 * similar to onChangeRefresh, but the compilation occurs in the background.
	 * While the compilation occurs, the ScalaScriptEngine.get(className)
	 * returns the existing version of the class without blocking.
	 *
	 * Please call refresh before using the engine for the first time.
	 *
	 * Before exiting, please call shutdown to shutdown the compilation thread
	 */
	def onChangeRefreshAsynchronously(sourcePath: File, recheckEveryInMillis: Long): ScalaScriptEngine with OnChangeRefresh with RefreshAsynchronously =
		onChangeRefreshAsynchronously(defaultConfig(sourcePath), recheckEveryInMillis)

	def onChangeRefreshAsynchronously(config: Config, recheckEveryInMillis: Long): ScalaScriptEngine with OnChangeRefresh with RefreshAsynchronously =
		new ScalaScriptEngine(config) with OnChangeRefresh with RefreshAsynchronously {
			val recheckEveryMillis: Long = recheckEveryInMillis
		}
}