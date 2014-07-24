package com.googlecode.scalascriptengine

import scala.reflect.ClassTag

/**
 * provides easy access to instance construction
 *
 * @author kostantinos.kougios
 *
 * 5 Jan 2012
 */
class Constructors[T](clz: Class[T]) {

	def newInstance = clz.newInstance

	def newInstance[P1](p1: P1)(implicit p1m: ClassTag[P1]): T = {
		val c = clz.getConstructor(p1m.runtimeClass)
		c.newInstance(p1.asInstanceOf[Object])
	}

	def newInstance[P1, P2](p1: P1, p2: P2)(implicit p1m: ClassTag[P1], p2m: ClassTag[P2]): T = {
		val c = clz.getConstructor(p1m.runtimeClass, p2m.runtimeClass)
		c.newInstance(p1.asInstanceOf[Object], p2.asInstanceOf[Object])
	}

	def newInstance[P1, P2, P3](p1: P1, p2: P2, p3: P3)(implicit p1m: ClassTag[P1], p2m: ClassTag[P2], p3m: ClassTag[P3]): T = {
		val c = clz.getConstructor(p1m.runtimeClass, p2m.runtimeClass, p3m.runtimeClass)
		c.newInstance(p1.asInstanceOf[Object], p2.asInstanceOf[Object], p3.asInstanceOf[Object])
	}

	def newInstance[P1, P2, P3, P4](p1: P1, p2: P2, p3: P3, p4: P4)(implicit p1m: ClassTag[P1], p2m: ClassTag[P2], p3m: ClassTag[P3], p4m: ClassTag[P4]): T = {
		val c = clz.getConstructor(p1m.runtimeClass, p2m.runtimeClass, p3m.runtimeClass, p4m.runtimeClass)
		c.newInstance(p1.asInstanceOf[Object], p2.asInstanceOf[Object], p3.asInstanceOf[Object], p4.asInstanceOf[Object])
	}

	/**
	 * returns a function that can be used to instantiate the class using a no-arg constructor
	 */
	def constructor = newInstance _

	/**
	 * returns a function that can be used to instantiate the class using 1 arg constructor
	 */
	def constructorWith1Arg[P1](implicit p1m: ClassTag[P1]): P1 => T = {
		val c = clz.getConstructor(p1m.runtimeClass)
		(p1: P1) => c.newInstance(p1.asInstanceOf[Object])
	}

	/**
	 * returns a function that can be used to instantiate the class using 2 arg constructor
	 */
	def constructorWith2Args[P1, P2](implicit p1m: ClassTag[P1], p2m: ClassTag[P2]): (P1, P2) => T = {
		val c = clz.getConstructor(p1m.runtimeClass, p2m.runtimeClass)
		(p1: P1, p2: P2) => c.newInstance(p1.asInstanceOf[Object], p2.asInstanceOf[Object])
	}

	/**
	 * returns a function that can be used to instantiate the class using 3 arg constructor
	 */
	def constructorWith3Args[P1, P2, P3](implicit p1m: ClassTag[P1], p2m: ClassTag[P2], p3m: ClassTag[P3]): (P1, P2, P3) => T = {
		val c = clz.getConstructor(p1m.runtimeClass, p2m.runtimeClass, p3m.runtimeClass)
		(p1: P1, p2: P2, p3: P3) => c.newInstance(p1.asInstanceOf[Object], p2.asInstanceOf[Object], p3.asInstanceOf[Object])
	}

	/**
	 * returns a function that can be used to instantiate the class using 3 arg constructor
	 */
	def constructorWith4Args[P1, P2, P3, P4](implicit p1m: ClassTag[P1], p2m: ClassTag[P2], p3m: ClassTag[P3], p4m: ClassTag[P4]): (P1, P2, P3, P4) => T = {
		val c = clz.getConstructor(p1m.runtimeClass, p2m.runtimeClass, p3m.runtimeClass, p4m.runtimeClass)
		(p1: P1, p2: P2, p3: P3, p4: P4) => c.newInstance(p1.asInstanceOf[Object], p2.asInstanceOf[Object], p3.asInstanceOf[Object], p4.asInstanceOf[Object])
	}
}