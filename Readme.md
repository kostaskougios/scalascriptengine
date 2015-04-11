### Description ###
This library dynamically compiles scala source files and loads them as classes. Changed scala files will be recompiled and the changed class with be loaded. Multiple source paths are supported as well as compilation class path and class loading class paths (so that the scripts can load extra libraries).

Classpath detection can be automatic (effectively using the classpath of the caller) or manual.

Different compilation and refreshing strategies are provided to suit various purposes.

### News ###
  * 08/06/2014 : v1.3.10 for scala 2.11 is now available, fixing the artifactId for sbt and scala version
  * 15/05/2014 : v1.3.9 for scala 2.11 is now available.
  * 05/03/2014 : v1.3.9 is available, bug fixes for Eval, and now eval is capable of taking arguments with generics. Also source paths can now be a set of files instead of directories.
  * 26/01/2014 : v1.3.8 is available, dropped dependency to time\_2.9.1 and fixed compilation issue within eclipse.
  * 21/10/2013 : v1.3.7 is available with optimizations during class loading
  * 06/10/2013 : v1.3.6 is available with class registry and for scala 2.10.3
  * 23/07/2013 : v1.3.5 is available with  error reporting fixes and only for scala 2.10.2
  * 30/06/2013 : v1.3.4 is available, with error reporting fixes and only for scala 2.10.2
  * 24/04/2013 : v1.3.2 is available with class loading listeners
  * 02/03/2013 : v1.3.1 is available with better error reporting
  * 20/02/2013 : v1.3.0 is available with better support for using ScalaScriptEngine within an IDE and multiple target class folders.
  * 09/02/2013 : v1.2.1 is now available with better compilation error reporting and sbt compatibility fix.
  * 23/01/2013 : snapshots of v1.2.1 are available in sonatype snapshot repo. Those fix sbt issues.
  * 12/01/2013 : v1.2.0 for scala 2.10.0 is now available
  * 16/10/2012 : v1.2.0 : Sandbox, better eval() and compilation for scala 2.9.2 and 2.10.0-M7 . For Sandbox please look at the end of this page.
  * 25/08/2012 : v1.1.0 : this has support for evaluating scala code from a String.
  * 22/07/2012 : migrated to git
  * 19/07/2012 : v1.0.0 : v0.6.4 is promoted to v1.0.0
[more...](wiki/News.md)

### Examples ###

[Please click to view examples](https://github.com/kostaskougios/scalascriptengine/tree/master/src/test/scala/examples)

[eval(): Evaluating scala code from a String](https://github.com/kostaskougios/scalascriptengine/blob/master/src/test/scala/com/googlecode/scalascriptengine/EvalCodeSuite.scala)

### Discuss ###

at [http://groups.google.com/group/scala-script-engine](http://groups.google.com/group/scala-script-engine)

### Download ###

Please use the maven repository to download the required jar, sources and javadocs :
[Download](https://oss.sonatype.org/content/repositories/releases/com/googlecode/scalascriptengine/scalascriptengine/)

### Maven ###

Both scalascriptengine, scala-reflect and scala-compiler must be added as dependencies :

```
<dependency>
	<groupId>com.googlecode.scalascriptengine</groupId>
	<artifactId>scalascriptengine_${scala.version}</artifactId>
	<version>1.3.10</version>
</dependency>
<dependency>
	<groupId>org.scala-lang</groupId>
	<artifactId>scala-compiler</artifactId>
	<version>${scala.version}</version>
</dependency>
<dependency>
	<groupId>org.scala-lang</groupId>
	<artifactId>scala-reflect</artifactId>
	<version>${scala.version}</version>
</dependency>
```

Please add the sonatype releases repository to your repositories:

```
<repositories>
        <repository>
                <id>sonatype.releases</id>
                <url>https://oss.sonatype.org/content/repositories/releases/</url>
        </repository>
</repositories>
```

### sbt ###

```
"com.googlecode.scalascriptengine" %% "scalascriptengine" % "1.3.10",
"org.scala-lang" % "scala-compiler" % "2.11.1"

```

NOTE: add

fork:=true

to your build.sbt because sbt seems to create issues with the scala compiler.
 
### Usage ###

This is not the most efficient usage of the library, but is the one with the most expected behavior:

```
// sourceDir is the folder with the scala source files
val sse = ScalaScriptEngine.onChangeRefresh(sourceDir)
// get the class which should extend statically
// compiled trait ClzTrait
val clzTraitClass=sse.get[ClzTrait]("my.dynamic.Clz")
// or get a new instance
val clzTrait=sse.newInstance[ClzTrait]("my.dynamic.Clz")
```

Please note that scala classes that are going to be requested from ScalaScriptEngine, should be declared in a synonymous scala file, i.e. my.Foo should be under my/Foo.scala in order for change detection to work.

### Avoiding compilation during development ###

The engine can be configured to use classes as they are compiled by an IDE.

```
val sse=if(is running for production)
...normal script engine initialization for production env
else
	new ScalaScriptEngine(Config(sourcePaths = List(
		SourcePath(...source folder, i.e. src/main/scala..., ... existing class folder, i.e. target/classes)
	))) with DevUseIDECompiledClassesOnly
```

Now scripts can be recompiled within the IDE and, without restarting your java app, the compiled classes will be reloaded on every sse.get or sse.newInstance.

NOTE: this frequently throws away a classloader and it is not recommended for production as it is slow and will cause a PermGen issue. But it is very handy during development.

### Examples ###

[Please click to view examples](https://github.com/kostaskougios/scalascriptengine/tree/master/src/test/scala/examples)

### How does it work ###

The ScalaScriptEngine class works by keeping versions of compiled source directories. Version 1 can be loaded during initialization of the engine or during the request for the first script. After that, there are different policies to refresh the changed source files:

  * **manual** : the client of the engine manually calls ScalaScriptEngine.refresh to check & recompile changed classes in the source directories.

  * **on-change-refresh** : as soon as the src file for a requested class changes, the source dirs are recompiled (only changed files). The code requesting for the changed class blocks till compilation completes

  * **on-change-refresh-async**: as soon as the src file for a requested class changes, the source dirs are recompiled (only changed files). The code requesting for the changed class resumes execution but uses an old version of the class till compilation completes. This method scales up better for i.e. servers that need to process hundreds of requests per second and blocking till compilation completes is not an option.

  * **timed refresh**: a background thread periodically scans the source folders for changes and recompiles them. During recompilation, old version classes are returned by the engine but as soon as compilation completes the new version classes are used.

In case of compilation errors, the previous version remains in use.

### Sandbox ###

Please view the test suites:

  * [policy file](https://github.com/kostaskougios/scalascriptengine/blob/master/src/test/scala/com/googlecode/scalascriptengine/SandboxSuite.scala)
  * [Example 2](https://github.com/kostaskougios/scalascriptengine/blob/master/src/test/scala/com/googlecode/scalascriptengine/SandboxAllowOnlySuite.scala)


ScalaScriptEngine can be configured to work with a Java sandbox and in addition offers extra help in terms of SecureManager and limited classloading for scripts.

### Sandbox and SecureManager ###

create a policy file:
```
grant codeBase "file:${user.home}/-" {
	permission java.security.AllPermission;
};

grant codeBase "${script.classes}/-" {
	permission java.io.FilePermission	"/home","read";
};
```

Register a SecurityManager with the help of SSESecurityManager:
```
import com.googlecode.scalascriptengine._

// create the default config with a Source Dir. The temp directory where
// the compiled classes are stored is in the OS tmp folder.
val config = ScalaScriptEngine.defaultConfig(sourceDir)
// We are now going to create a security manager with the test.policy
// file. We need to fill the placeholders of test.policy
System.setProperty("script.classes", config.outputDir.toURI.toString)
System.setProperty("java.security.policy", new File("test.policy").toURI.toString)
val sseSM = new SSESecurityManager(new SecurityManager)
System.setSecurityManager(sseSM)

```

The SSESecurityManager is by default not active. So the rest of the java code will run like if not under a security manager. The SSESecurityManager activates the delegate SecurityManager as follows:

```

val sse = ScalaScriptEngine.onChangeRefresh(config, 5)
sse.deleteAllClassesInOutputDirectory
sse.refresh

sseSM.secured {
	// now the delegated SecurityManager is active and hence test.policy is active
	val tct = sse.newInstance[TestClassTrait]("test.TryFile")
	tct.result should be === "directory"
}
```

Please note: SSESecurityManager can be bypassed and a global SecurityManager can be installed for both the main scala app and the scripts. SSESecurityManager is provided as a helper to avoid running all code under a security manager.

### Configuring limited access to loaded classes ###

Scripts can be limited to i.e. not be able to load classes from specific packages. The decision is just a function (packageName,fullClassName)=>Boolean. If true, access to `fullClassName` class is allowed otherwise `AccessControlException` is thrown.

The following example allows access only to certain packages and i.e. Threads can't be created by the scripts (except ofcourse if one of the allowed packages contains a class that creates threads):

```
val allowedPackages = Set(
	"java.lang",
	"scala",
	"com.googlecode.scalascriptengine")
val config = ScalaScriptEngine.defaultConfig(sourceDir).copy(
	classLoaderConfig = ClassLoaderConfig.default.copy(
		allowed = { (pckg, name) =>
			allowedPackages(pckg) || pckg == "test"
		}
	)
)
val sse = ScalaScriptEngine.onChangeRefresh(config, 5)
sse.deleteAllClassesInOutputDirectory
sse.refresh

val t = sse.newInstance[TestClassTrait]("test.TryPackage")
// if test.TryPackage tries to use a class not in the allowed
// packages, an AccessControlException will be thrown 
t.result

```

Please note this mechanism works independently of a security manager. No security manager is required as this mechanism works during classloading.