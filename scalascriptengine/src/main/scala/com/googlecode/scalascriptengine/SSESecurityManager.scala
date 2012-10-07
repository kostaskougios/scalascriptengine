package com.googlecode.scalascriptengine

import java.security.Permission

/**
 * @author kostantinos.kougios
 *
 * 7 Oct 2012
 */
class SSESecurityManager(securityManager: Option[SecurityManager]) extends SecurityManager {

	// we can't do securityManager.foreach within checkPermission cause it causes an infinite loop
	// while trying to load resources. But the below method works:
	private val sm = if (securityManager.isDefined) securityManager.get else null

	private var enabled = new InheritableThreadLocal[Boolean]
	override def checkPermission(perm: Permission) {
		val e = enabled.get
		if (enabled.get && sm != null)
			sm.checkPermission(perm)
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