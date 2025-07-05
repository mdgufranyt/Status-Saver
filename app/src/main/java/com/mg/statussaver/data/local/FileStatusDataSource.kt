package com.mg.statussaver.data.local

import com.mg.statussaver.domain.model.Status_domain
import java.text.SimpleDateFormat
import java.util.*

class FileStatusDataSource {
    // For demonstration: in-memory data using domain model directly
    private val recentStatuses = mutableListOf(
        Status_domain("1", "https://placekitten.com/200/200", false, null, getCurrentTime(), false, null),
        Status_domain("2", "https://placekitten.com/201/201", false, null, getCurrentTime(), false, null),
        Status_domain(
            "3",
            "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
            true,
            "0:15",
            getCurrentTime(),
            false,
            null
        )
    )
    private val savedStatuses = mutableListOf<Status_domain>()

    fun getRecentStatuses(): List<Status_domain> = recentStatuses

    fun getSavedStatuses(): List<Status_domain> = savedStatuses

    fun saveStatus(status: Status_domain) {
        if (!savedStatuses.any { it.id == status.id }) {
            savedStatuses.add(status)
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
