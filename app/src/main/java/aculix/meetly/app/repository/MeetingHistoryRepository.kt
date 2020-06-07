package aculix.meetly.app.repository

import aculix.meetly.app.db.MeetingDao
import aculix.meetly.app.model.Meeting
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MeetingHistoryRepository(private val meetingDao: MeetingDao) {

    fun getMeetingHistory(): LiveData<List<Meeting>> =
        meetingDao.getAllMeetings()

    suspend fun addMeetingToDb(meeting: Meeting) = withContext(Dispatchers.IO) {
        meetingDao.insertMeeting(meeting)
    }
}