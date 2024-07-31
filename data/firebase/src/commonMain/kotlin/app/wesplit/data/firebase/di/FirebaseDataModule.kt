package app.wesplit.data.firebase

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

fun firebaseDataModule() = FirebaseDataModule().module

@Module
@ComponentScan("app.wesplit.data.firebase")
class FirebaseDataModule