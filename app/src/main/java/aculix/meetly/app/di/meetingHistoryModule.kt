package aculix.meetly.app.di

import aculix.meetly.app.repository.MeetingHistoryRepository
import aculix.meetly.app.viewmodel.MeetingHistoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val meetingHistoryModule = module {

    single { MeetingHistoryRepository(get()) }
    viewModel { MeetingHistoryViewModel(get()) }

}
