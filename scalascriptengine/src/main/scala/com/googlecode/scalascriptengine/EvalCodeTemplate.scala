package com.googlecode.scalascriptengine

import com.googlecode.classgenerator.ReflectionManager
import java.io.File
import java.util.UUID
import scala.io.Source
import java.io.FileWriter

/**
 * @author kostantinos.kougios
 *
 * 20 Aug 2012
 */

class EvalCode[T](clz: Class[T], argNames: Iterable[String], body: String) {
	def this(argNames: Iterable[String], body: String)(implicit m: ClassManifest[T]) = this(m.erasure.asInstanceOf[Class[T]], argNames, body)

	import EvalCode._

	private val reflectionManager = new ReflectionManager
	private val srcFolder = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID.toString)
	if (!srcFolder.mkdir) throw new IllegalStateException("can't create temp folder %s".format(srcFolder))
	private val sse = ScalaScriptEngine.onChangeRefresh(srcFolder)

	private val applyMethod = reflectionManager.method(clz, "apply").getOrElse(throw new IllegalArgumentException("class %s doesn't have an apply method".format(clz)))

	if (argNames.size != applyMethod.getParameterTypes.length) throw new IllegalArgumentException("argNames " + argNames + " not equal to the number of parameters of " + applyMethod)

	private val templateTop = """
		package eval
		
		class Eval extends %s {
			override def apply(%s):%s = { %s }
		}
		""".format(
		clz.getName,
		(argNames zip applyMethod.getParameterTypes).map {
			case (pName, pt) =>
				val typeName = typesToName.getOrElse(pt, pt.getName)
				pName + " : " + typeName
		}.mkString(","),
		typesToName.getOrElse(applyMethod.getReturnType, applyMethod.getReturnType.getName),
		body
	)

	private val src = new FileWriter(new File(srcFolder, "Eval.scala"))
	try {
		src.write(templateTop)
	} finally {
		src.close()
	}

	// the Class[T]
	val generatedClass = sse.get[T]("eval.Eval")
	// creates a new instance of the evaluated class
	def newInstance: T = generatedClass.newInstance
}

object EvalCode {
	val typesToName = Map[Class[_], String](
		classOf[Int] -> "Int"
	)
}