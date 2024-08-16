package app.wesplit

import android.app.Application
import app.wesplit.di.ActivityProvider

class MainApplication : Application() {
    lateinit var activityProvider: ActivityProvider

    override fun onCreate() {
        super.onCreate()
        activityProvider = ActivityProvider(this)
    }
}
