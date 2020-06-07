package aculix.meetly.app.di

import aculix.meetly.app.repository.MainRepository
import aculix.meetly.app.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    single { MainRepository(get()) }
    viewModel { MainViewModel(get()) }

}
