package app.wesplit

import android.app.Application
import app.wesplit.di.ActivityProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class MainApplication : Application() {
    lateinit var activityProvider: ActivityProvider

    override fun onCreate() {
        super.onCreate()
        Firebase.firestore.setLoggingEnabled(BuildConfig.DEBUG)
        activityProvider = ActivityProvider(this)
    }
}
