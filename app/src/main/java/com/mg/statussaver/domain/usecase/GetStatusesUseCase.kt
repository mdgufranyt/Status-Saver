package com.mg.statussaver.domain.usecase

import com.mg.statussaver.domain.repository.StatusRepository
import com.mg.statussaver.presentation.screens.home.StatusItem
import javax.inject.Inject

/**
 * Use case for fetching statuses - either WhatsApp statuses or downloaded statuses
 */
class GetStatusesUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    /**
     * Fetches statuses based on the requested type.
     *
     * @param saved If true, returns downloaded statuses. If false, returns WhatsApp and WhatsApp Business statuses.
     * @return List of StatusItem objects
     */
    suspend operator fun invoke(saved: Boolean = false): List<StatusItem> =
        if (saved) {
            repository.getDownloadedStatuses()
        } else {
            val whatsAppStatuses = repository.getWhatsAppStatuses()
            val businessStatuses = repository.getWhatsAppBusinessStatuses()

            // Combine and sort by timestamp (newest first)
            (whatsAppStatuses + businessStatuses).sortedByDescending { it.timestamp }
        }
}