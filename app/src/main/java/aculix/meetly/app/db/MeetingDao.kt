package aculix.meetly.app.db

import aculix.meetly.app.model.Meeting
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Dao to save and access the meetings done by the user
 */
@Dao
interface MeetingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: Meeting): Long

    @Query("SELECT * FROM meetings ORDER BY timeInMillis DESC")
    fun getAllMeetings(): LiveData<List<Meeting>>

    @Query("DELETE FROM meetings where  code in (:meetingCodeList)")
    suspend fun deleteMultipleMeetings(meetingCodeList: List<String>)
}