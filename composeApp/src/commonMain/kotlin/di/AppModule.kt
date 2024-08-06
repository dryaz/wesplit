package di

import group.list.GroupListViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val provideviewModelModule = module {
    viewModelOf(::GroupListViewModel)
}

fun appModule() = listOf(
    provideviewModelModule,
    module { single<CoroutineDispatcher> { Dispatchers.Main } }
)
