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

private class EvalCodeImpl[T](clz: Class[T], typeArgs: List[Class[_]], argNames: Iterable[String], body: String) extends EvalCode[T] {

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
		if (typeArgs.isEmpty) "" else "[" + typeArgs.map { e =>
			typesToName.getOrElse(e, e.getName)
		}.mkString(",") + "]",
		// params
		(argNames zip typeArgs).map {
			case (pName, e) =>
				val typeName = typesToName.getOrElse(e, e.getName)
				pName + " : " + typeName
		}.mkString(","),
		// return type
		{
			val last = typeArgs.last
			typesToName.getOrElse(last, last.getName)
		},
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

	def apply[T](clz: Class[T], typeArgs: List[Class[_]], argNames: Iterable[String], body: String): EvalCode[T] =
		new EvalCodeImpl(clz, typeArgs, argNames, body)

	def apply[R](body: String): EvalCode[() => R] =
		apply(classOf[() => R], Nil, Nil, body)

	def apply[A1, R](
		arg1Var: String,
		body: String)(
			implicit m1: ClassManifest[A1],
			r: ClassManifest[R]): EvalCode[A1 => R] =
		apply(classOf[A1 => R], List(m1.erasure, r.erasure), List(arg1Var), body)

	def apply[A1, A2, R](
		arg1Var: String,
		arg2Var: String,
		body: String)(
			implicit m1: ClassManifest[A1],
			m2: ClassManifest[A2],
			r: ClassManifest[R]): EvalCode[(A1, A2) => R] =
		apply(classOf[(A1, A2) => R], List(m1.erasure, m2.erasure, r.erasure), List(arg1Var, arg2Var), body)

	def apply[A1, A2, A3, R](
		arg1Var: String,
		arg2Var: String,
		arg3Var: String,
		body: String)(
			implicit m1: ClassManifest[A1],
			m2: ClassManifest[A2],
			m3: ClassManifest[A3],
			r: ClassManifest[R]): EvalCode[(A1, A2, A3) => R] =
		apply(classOf[(A1, A2, A3) => R], List(m1.erasure, m2.erasure, m3.erasure, r.erasure), List(arg1Var, arg2Var, arg3Var), body)
}