package app.wesplit.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import app.wesplit.MainApplication

class ActivityProvider(
    private val application: MainApplication,
) {
    var activeActivity: Activity? = null

    init {
        application.registerActivityLifecycleCallbacks(
            object :
                Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    p1: Bundle?,
                ) {
                    // Nothing
                }

                override fun onActivityStarted(activity: Activity) {
                    // Nothing
                }

                override fun onActivityResumed(activity: Activity) {
                    activeActivity = activity
                }

                override fun onActivityPaused(activity: Activity) {
                    activeActivity = null
                }

                override fun onActivityStopped(activity: Activity) {
                    // Nothing
                }

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    p1: Bundle,
                ) {
                    // Nothing
                }

                override fun onActivityDestroyed(activity: Activity) {
                    // Nothing
                }
            },
        )
    }
}
