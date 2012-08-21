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

private class EvalCodeImpl[T](clz: Class[T], typeArgs: List[ClassManifest[_]], argNames: Iterable[String], body: String) extends EvalCode[T] {

	import EvalCode.typesToName

	private val reflectionManager = new ReflectionManager
	private val srcFolder = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID.toString)
	if (!srcFolder.mkdir) throw new IllegalStateException("can't create temp folder %s".format(srcFolder))
	private val sse = ScalaScriptEngine.onChangeRefresh(srcFolder)

	private val templateTop = """
		package eval
		
		class Eval extends %s%s {
			override def apply(%s):%s = { %s }
		}
		""".format(
		// super class name
		clz.getName,
		// type args
		if (typeArgs.isEmpty) "" else "[" + typeArgs.map { ta =>
			val e = ta.erasure
			typesToName.getOrElse(e, e.getName)

		}.mkString(",") + "]",
		// params
		(argNames zip typeArgs).map {
			case (pName, cm) =>
				val e = cm.erasure
				val typeName = typesToName.getOrElse(e, e.getName)
				pName + " : " + typeName
		}.mkString(","),
		// return type
		typesToName.getOrElse(typeArgs.last.erasure, typeArgs.last.erasure.getName),
		// body
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

/**
 * a scala-code evaluator
 */
trait EvalCode[T] {
	// the Class[T]
	val generatedClass: Class[T]
	// creates a new instance of the evaluated class
	def newInstance: T
}

object EvalCode {
	private[scalascriptengine] val typesToName = Map[Class[_], String](
		classOf[Int] -> "Int",
		classOf[Float] -> "Float",
		classOf[Double] -> "Double",
		classOf[Boolean] -> "Boolean",
		classOf[Short] -> "Short",
		classOf[Byte] -> "Byte",
		classOf[Char] -> "Char",
		classOf[Long] -> "Long"
	)

	def apply[T](clz: Class[T], typeArgs: List[ClassManifest[_]], argNames: Iterable[String], body: String): EvalCode[T] =
		new EvalCodeImpl(clz, typeArgs, argNames, body)

	def apply[T](argNames: Iterable[String], body: String)(implicit m: ClassManifest[T]): EvalCode[T] =
		apply(m.erasure.asInstanceOf[Class[T]], m.typeArguments.asInstanceOf[List[ClassManifest[_]]], argNames, body)
}