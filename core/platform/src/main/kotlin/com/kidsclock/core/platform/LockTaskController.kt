package com.kidsclock.core.platform

import android.app.Activity
import android.app.ActivityManager

/**
 * Wraps Android's screen-pinning / lock-task API (ADR 0004) so the run screen can request and
 * release the "child cannot leave the app" state. Which underlying mode is used (screen pinning
 * vs. device-owner lock task) is Phase 1 spike (a); this interface covers both, since both are
 * driven the same way from an [Activity].
 */
interface LockTaskController {
    fun start(activity: Activity)

    fun stop(activity: Activity)

    fun isActive(activity: Activity): Boolean
}

class AndroidLockTaskController : LockTaskController {
    override fun start(activity: Activity) {
        activity.startLockTask()
    }

    override fun stop(activity: Activity) {
        activity.stopLockTask()
    }

    override fun isActive(activity: Activity): Boolean {
        val activityManager = activity.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
    }
}
