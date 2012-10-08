package com.googlecode.scalascriptengine

import java.security.Permission

/**
 * @author kostantinos.kougios
 *
 * 7 Oct 2012
 */
class SSESecurityManager(securityManager: SecurityManager) extends SecurityManager {

	if (securityManager == null) throw new NullPointerException("securityManager shouldn't be null")

	private var enabled = new InheritableThreadLocal[Boolean]

	override def checkPermission(perm: Permission) {
		val e = enabled.get
		if (enabled.get)
			securityManager.checkPermission(perm)
	}

	def secured[R](f: => R) = {
		enabled.set(true)
		try {
			f
		} finally {
			enabled.set(false)
		}
	}
}