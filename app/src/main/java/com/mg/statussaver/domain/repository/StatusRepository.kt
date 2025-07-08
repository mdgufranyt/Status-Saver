package com.mg.statussaver.domain.repository

import android.content.Context
import com.mg.statussaver.presentation.screens.home.StatusItem

/**
 * Domain layer interface for StatusRepository
 * This follows clean architecture principles by defining the contract in the domain layer
 */
interface StatusRepository {
    // Status fetching methods
    suspend fun getWhatsAppStatuses(): List<StatusItem>
    suspend fun getWhatsAppBusinessStatuses(): List<StatusItem>
    suspend fun getDownloadedStatuses(): List<StatusItem>

    // Download result class for better feedback
    data class DownloadResult(
        val success: Boolean,
        val savedPath: String? = null,
        val errorMessage: String? = null
    )

    // Status operations
    suspend fun downloadStatus(status: StatusItem): DownloadResult
    suspend fun deleteDownloadedStatus(status: StatusItem): Boolean
    suspend fun shareStatus(context: Context, status: StatusItem)
}