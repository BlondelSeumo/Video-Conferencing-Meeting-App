package aculix.meetly.app.db

import aculix.meetly.app.model.Meeting
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Meeting::class], version = 1)
abstract class MeetlyDB : RoomDatabase() {

    abstract fun meetingDao(): MeetingDao

}