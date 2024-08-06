package di

import group.GroupListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val provideviewModelModule = module {
    viewModelOf(::GroupListViewModel)
}

fun appModule() = listOf(
    provideviewModelModule
)
