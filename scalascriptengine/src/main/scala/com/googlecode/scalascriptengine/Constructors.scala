package com.googlecode.scalascriptengine

/**
 * provides easy access to instance construction
 *
 * @author kostantinos.kougios
 *
 * 5 Jan 2012
 */
class Constructors[T](clz: Class[T]) {
	def newInstance = clz.newInstance
	def newInstance[P1](p1: P1)(implicit p1m: ClassManifest[P1]): T = {
		val c = clz.getConstructor(p1m.erasure)
		c.newInstance(Array(p1.asInstanceOf[Object]): _*)
	}
	def newInstance[P1, P2](p1: P1, p2: P2)(implicit p1m: ClassManifest[P1], p2m: ClassManifest[P2]): T = {
		val c = clz.getConstructor(p1m.erasure, p2m.erasure)
		c.newInstance(Array(p1.asInstanceOf[Object], p2.asInstanceOf[Object]): _*)
	}
	def newInstance[P1, P2, P3](p1: P1, p2: P2, p3: P3)(implicit p1m: ClassManifest[P1], p2m: ClassManifest[P2], p3m: ClassManifest[P3]): T = {
		val c = clz.getConstructor(p1m.erasure, p2m.erasure, p3m.erasure)
		c.newInstance(Array(p1.asInstanceOf[Object], p2.asInstanceOf[Object], p3.asInstanceOf[Object]): _*)
	}
	def newInstance[P1, P2, P3, P4](p1: P1, p2: P2, p3: P3, p4: P4)(implicit p1m: ClassManifest[P1], p2m: ClassManifest[P2], p3m: ClassManifest[P3], p4m: ClassManifest[P4]): T = {
		val c = clz.getConstructor(p1m.erasure, p2m.erasure, p3m.erasure, p4m.erasure)
		c.newInstance(Array(p1.asInstanceOf[Object], p2.asInstanceOf[Object], p3.asInstanceOf[Object], p4.asInstanceOf[Object]): _*)
	}
}