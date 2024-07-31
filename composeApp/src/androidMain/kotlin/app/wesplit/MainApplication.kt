package app.wesplit

import android.app.Application
import app.wesplit.data.firebase.domainModule
import app.wesplit.data.firebase.firebaseDataModule
import app.wesplit.di.ActivityProvider
import app.wesplit.di.AndroidAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(
                AndroidAppModule().module
                    + domainModule()
                    + firebaseDataModule()
                    + module(createdAtStart = true) { single { ActivityProvider(this@MainApplication) } }
            )
        }
    }
}
