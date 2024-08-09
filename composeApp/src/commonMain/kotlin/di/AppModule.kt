package di

import CommonAnalyticsManager
import app.wesplit.domain.model.AnalyticsManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

fun appModule() =
    listOf(
        // TODO: Correct dispatcher for IO
        module { single<CoroutineDispatcher> { Dispatchers.Main } },
        module { single<AnalyticsManager> { CommonAnalyticsManager() } },
    )
