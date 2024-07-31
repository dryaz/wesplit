package app.wesplit.data.firebase

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

fun domainModule() = DomainModule().module

@Module
@ComponentScan("app.wesplit.domain")
class DomainModule