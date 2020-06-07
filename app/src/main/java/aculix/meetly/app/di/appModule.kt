package aculix.meetly.app.di

import aculix.meetly.app.db.MeetlyDB
import android.app.Application
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {

    single { provideRoomDatabase(androidApplication()) }
    single { provideMeetingDao(get()) }
}

private fun provideRoomDatabase(androidApplication: Application) =
    Room.databaseBuilder(androidApplication, MeetlyDB::class.java, "meetly-db").build()

private fun provideMeetingDao(meetlyDB: MeetlyDB) =
    meetlyDB.meetingDao()