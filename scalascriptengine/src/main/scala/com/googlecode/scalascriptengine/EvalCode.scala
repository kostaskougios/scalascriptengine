package com.googlecode.scalascriptengine

import java.io.File
import java.util.UUID
import java.io.FileWriter
import reflect.runtime.universe._

/**
 * @author kostantinos.kougios
 *
 *         20 Aug 2012
 */

private class EvalCodeImpl[T](
	clz: Class[T],
	typeArgs: List[TypeTag[_]],
	argNames: Iterable[String],
	body: String,
	classLoaderConfig: ClassLoaderConfig
	)
	extends EvalCode[T]
{
	private val srcFolder = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID.toString)
	if (!srcFolder.mkdir) throw new IllegalStateException("can't create temp folder %s".format(srcFolder))

	private val config = ScalaScriptEngine.defaultConfig(srcFolder).copy(classLoaderConfig = classLoaderConfig)
	private val sse = ScalaScriptEngine.onChangeRefresh(config, 0)

	private val templateTop = """
		class Eval extends %s%s {
			override def apply(%s):%s = { %s }
		}
							  """.format(
	// super class name
	clz.getName,
	// type args
	if (typeArgs.isEmpty) ""
	else "[" + typeArgs.map {
		tt =>
			toString(tt)
	}.mkString(",") + "]",
	// params
	(argNames zip typeArgs).map {
		case (pName, tt) =>
			pName + " : " + toString(tt)
	}.mkString(","), {
		val last = typeArgs.last
		toString(last)
	},
	// body
	body
	)

	private def toString(tt: TypeTag[_]) = {
		val t = tt.tpe
		t.typeSymbol.name
	}

	private val src = new FileWriter(new File(srcFolder, "Eval.scala"))
	try {
		src.write(templateTop)
	} finally {
		src.close()
	}

	// the Class[T]
	val generatedClass = sse.get[T]("Eval")

	// creates a new instance of the evaluated class
	def newInstance: T = generatedClass.newInstance
}

/**
 * a scala-code evaluator
 */
trait EvalCode[T]
{
	// the Class[T]
	val generatedClass: Class[T]

	// creates a new instance of the evaluated class
	def newInstance: T
}

object EvalCode
{
	def apply[T](clz: Class[T], typeArgs: List[TypeTag[_]], argNames: Iterable[String], body: String, classLoaderConfig: ClassLoaderConfig): EvalCode[T] =
		new EvalCodeImpl(clz, typeArgs, argNames, body, classLoaderConfig)

	def withoutArgs[R](body: String, classLoaderConfig: ClassLoaderConfig = ClassLoaderConfig.Default)(implicit retTag: TypeTag[R]) =
		apply(classOf[() => R], List(retTag), Nil, body, classLoaderConfig)

	def with1Arg[A1, R](
		arg1Var: String,
		body: String,
		classLoaderConfig: ClassLoaderConfig = ClassLoaderConfig.Default
		)(
		implicit arg1Tag: TypeTag[A1],
		retTag: TypeTag[R]
		) =
		apply(classOf[A1 => R], List(arg1Tag, retTag), List(arg1Var), body, classLoaderConfig)

	def with2Args[A1, A2, R](
		arg1Var: String,
		arg2Var: String,
		body: String,
		classLoaderConfig: ClassLoaderConfig = ClassLoaderConfig.Default
		)(
		implicit arg1Tag: TypeTag[A1],
		arg2Tag: TypeTag[A2],
		retTag: TypeTag[R]
		) =
		apply(classOf[(A1, A2) => R], List(arg1Tag, arg2Tag, retTag), List(arg1Var, arg2Var), body, classLoaderConfig)

	def with3Args[A1, A2, A3, R](
		arg1Var: String,
		arg2Var: String,
		arg3Var: String,
		body: String,
		classLoaderConfig: ClassLoaderConfig = ClassLoaderConfig.Default
		)(
		implicit arg1Tag: TypeTag[A1],
		arg2Tag: TypeTag[A2],
		arg3Tag: TypeTag[A3],
		retTag: TypeTag[R]
		) =
		apply(classOf[(A1, A2, A3) => R], List(arg1Tag, arg2Tag, arg3Tag, retTag), List(arg1Var, arg2Var, arg3Var), body, classLoaderConfig)
}