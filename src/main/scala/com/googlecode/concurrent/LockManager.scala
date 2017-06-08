package com.googlecode.concurrent

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.{Lock, ReadWriteLock, ReentrantLock, ReentrantReadWriteLock}

import org.joda.time.DateTime

/**
  * manages locks.
  *
  * @author kostantinos.kougios
  *
  *         7 Nov 2011
  */
object LockManager
{
	def reentrantLock = new LockEx(new ReentrantLock)

	def readWriteLock = new ReadWriteLockEx(new ReentrantReadWriteLock)
}

protected class ReadWriteLockEx(val lock: ReadWriteLock)
{
	private val readLock = new LockEx(lock.readLock())
	private val writeLock = new LockEx(lock.writeLock())

	def readLockAndDo[T](f: => T): T = readLock.lockAndDo(f)

	def readLockInterruptiblyAndDo[T](f: => T): T = readLock.lockInterruptiblyAndDo(f)

	def tryReadLockAndDo[T](f: => T): Option[T] = readLock.tryLockAndDo(f)

	def tryReadLockAndDo[T](when: DateTime)(f: => T): Option[T] =
		tryReadLockAndDo(when.getMillis - System.currentTimeMillis, TimeUnit.MILLISECONDS)(f)

	def tryReadLockAndDo[T](time: Long, unit: TimeUnit)(f: => T): Option[T] = readLock.tryLockAndDo(time, unit)(f)

	def writeLockAndDo[T](f: => T): T = writeLock.lockAndDo(f)

	def writeLockInterruptiblyAndDo[T](f: => T): T = writeLock.lockInterruptiblyAndDo(f)

	def tryWriteLockAndDo[T](f: => T): Option[T] = writeLock.tryLockAndDo(f)

	def tryWriteLockAndDo[T](when: DateTime)(f: => T): Option[T] =
		tryWriteLockAndDo(when.getMillis - System.currentTimeMillis, TimeUnit.MILLISECONDS)(f)

	def tryWriteLockAndDo[T](time: Long, unit: TimeUnit)(f: => T): Option[T] = writeLock.tryLockAndDo(time, unit)(f)
}

protected class LockEx(val lock: Lock)
{

	def lockAndDo[T](f: => T): T = {
		lock.lock
		try {
			f
		} finally {
			lock.unlock
		}
	}

	def lockInterruptiblyAndDo[T](f: => T): T = {
		lock.lockInterruptibly
		try {
			f
		} finally {
			lock.unlock
		}
	}

	def tryLockAndDo[T](f: => T): Option[T] =
		if (lock.tryLock)
			try {
				Some(f)
			} finally {
				lock.unlock
			}
		else None

	def tryLockAndDo[T](when: DateTime)(f: => T): Option[T] =
		tryLockAndDo(when.getMillis - System.currentTimeMillis, TimeUnit.MILLISECONDS)(f)

	def tryLockAndDo[T](time: Long, unit: TimeUnit)(f: => T): Option[T] =
		if (lock.tryLock(time, unit))
			try {
				Some(f)
			} finally {
				lock.unlock
			}
		else None
}