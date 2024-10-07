package app.wesplit.data.firebase

import com.russhwolf.settings.Settings
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

fun firebaseDataModule() =
    FirebaseDataModule().module +
        module {
            single<Settings> { Settings() }
        }

@Module
@ComponentScan("app.wesplit.data.firebase")
class FirebaseDataModule
