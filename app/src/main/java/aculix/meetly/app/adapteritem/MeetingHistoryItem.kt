package aculix.meetly.app.adapteritem

import aculix.meetly.app.R
import aculix.meetly.app.databinding.ItemMeetingHistoryBinding
import aculix.meetly.app.model.Meeting
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class MeetingHistoryItem(val meeting: Meeting) :
    AbstractBindingItem<ItemMeetingHistoryBinding>() {

    override val type: Int
        get() = R.id.fastadapter_meeting_history_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ItemMeetingHistoryBinding {
        return ItemMeetingHistoryBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemMeetingHistoryBinding, payloads: List<Any>) {
        with(binding) {
            tvMeetingCode.text = meeting.code
            tvMeetingTime.text = root.context.getString(
                R.string.meeting_history_last_joined_time,
                TimeAgo.using(meeting.timeInMillis)
            )
        }

    }

    override fun unbindView(binding: ItemMeetingHistoryBinding) {
        with(binding) {
            tvMeetingCode.text = null
            tvMeetingTime.text = null
        }
    }


}