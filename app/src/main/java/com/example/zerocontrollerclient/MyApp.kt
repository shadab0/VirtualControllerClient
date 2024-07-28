package com.example.zerocontrollerclient

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate

class MyApp : Application() {
    internal var currentActivity: Activity? = null
    private var activityCount = 0
    private var isChangingOrientation = false
    override fun onCreate() {
        super.onCreate()
        // Force dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
                if (++activityCount == 1 && !isChangingOrientation) {
                    if (SharedObject.getSocket() != null)
                        startService(Intent(activity, ConnectionMonitorService::class.java))
                }
            }

            override fun onActivityStopped(activity: Activity) {
                isChangingOrientation = activity.isChangingConfigurations
                if (--activityCount == 0 && !isChangingOrientation) {
                    if (SharedObject.getSocket() != null)
                        stopService(Intent(activity, ConnectionMonitorService::class.java))
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
