package aculix.meetly.app.repository

import aculix.meetly.app.db.MeetingDao
import aculix.meetly.app.model.Meeting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepository(private val meetingDao: MeetingDao) {

    suspend fun addMeetingToDb(meeting: Meeting) = withContext(Dispatchers.IO) {
        meetingDao.insertMeeting(meeting)
    }

}