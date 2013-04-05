package org.openmrs.module.amrsreports.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Common task locking mechanism to ensure only one task relying on the lock can run at a given time
 */
public class AMRSReportsCommonTaskLock {

	static private AMRSReportsCommonTaskLock instance = null;
	static private Boolean locked;
	static private Class lastLockingClass;

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * initializes as unlocked
	 */
	public AMRSReportsCommonTaskLock() {
		this.locked = false;
		this.lastLockingClass = null;
	}

	/**
	 * gets the static instance of this class
	 */
	public static AMRSReportsCommonTaskLock getInstance() {
		if (instance == null)
			instance = new AMRSReportsCommonTaskLock();
		return instance;
	}

	/**
	 * informs the caller if a lock exists
	 */
	public Boolean isLocked() {
		return locked;
	}

	/**
	 * attempts to request a lock
	 *
	 * @should return true if a lock is obtained
	 * @should return false if already locked
	 */
	public Boolean getLock(Class lockingClass) {
		if (lockingClass == null) {
			log.warn("Could not grant lock to a null class");
			return false;
		}

		if (this.locked) {
			if (this.lastLockingClass != null)
				log.warn("Lock requested by " + lockingClass.getSimpleName() + ", but held by " + this.lastLockingClass.getSimpleName());
			else
				log.warn("Lock requested by " + lockingClass.getSimpleName() + ", but held by unknown class");
			return false;
		}

		this.lastLockingClass = lockingClass;
		this.locked = true;

		log.info("Lock granted to " + lockingClass.getSimpleName());

		return true;
	}

	/**
	 * releases the lock
	 *
	 * @should release a lock if the lockingClass matches
	 */
	public Boolean releaseLock(Class lockingClass) {
		if (this.lastLockingClass == lockingClass) {
			log.info("Releasing lock held by " + lockingClass.getSimpleName());
			this.locked = false;
		} else if (this.lastLockingClass != null)
			log.warn("Lock release requested by " + lockingClass.getSimpleName() + ", but held by " + this.lastLockingClass.getSimpleName());
		else
			log.warn("Lock release requested by " + lockingClass.getSimpleName() + ", but held by unknown class");

		return !this.locked;
	}

}