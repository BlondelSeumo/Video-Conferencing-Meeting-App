package aculix.meetly.app.viewmodel

import aculix.meetly.app.model.Meeting
import aculix.meetly.app.repository.MainRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    fun addMeetingToDb(meeting: Meeting) {
        viewModelScope.launch {
            repository.addMeetingToDb(meeting)
        }
    }

}