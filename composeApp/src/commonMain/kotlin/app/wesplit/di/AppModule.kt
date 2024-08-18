package app.wesplit.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

fun appModule() =
    listOf(
        // TODO: Correct dispatcher for IO
        module { single<CoroutineDispatcher> { Dispatchers.Main } },
    )
