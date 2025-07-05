package com.mg.statussaver.presentation.screens.home

import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    // Keep track of loading job to allow cancellation
    private var loadingJob: Job? = null

    // WhatsApp status folder paths for different Android versions
    private val whatsAppPaths = listOf(
        "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses", // Android 11+
        "/WhatsApp/Media/.Statuses" // Android 10 and below
    )

    private val whatsAppBusinessPaths = listOf(
        "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses", // Android 11+
        "/WhatsApp Business/Media/.Statuses" // Android 10 and below
    )

    // Supported file extensions
    private val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp")
    private val videoExtensions = listOf(".mp4", ".3gp", ".mkv", ".avi")

    fun setSelectedTab(index: Int) {
        _selectedTabIndex.value = index
        loadStatusFiles()
    }

    fun loadStatusFiles() {
        // Cancel any existing loading job
        loadingJob?.cancel()

        loadingJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                val allStatuses = withContext(Dispatchers.IO) {
                    // Check if coroutine is still active before proceeding
                    ensureActive()
                    fetchAllStatusFiles()
                }

                // Check if coroutine is still active before updating UI
                ensureActive()

                val filteredStatuses = when (_selectedTabIndex.value) {
                    0 -> allStatuses // Recent (all)
                    1 -> allStatuses.filter { it.type == MediaType.IMAGE } // Images only
                    2 -> allStatuses.filter { it.type == MediaType.VIDEO } // Videos only
                    else -> emptyList()
                }

                _uiState.value = if (filteredStatuses.isEmpty()) {
                    HomeUiState.Empty
                } else {
                    HomeUiState.Success(filteredStatuses)
                }
            } catch (e: Exception) {
                // Only update UI if coroutine is still active
                if (isActive) {
                    _uiState.value = HomeUiState.Error(e.message ?: "Failed to load status files")
                }
            }
        }
    }

    private suspend fun fetchAllStatusFiles(): List<StatusItem> = withContext(Dispatchers.IO) {
        val statusFiles = mutableListOf<StatusItem>()

        // Check if coroutine is still active before proceeding
        ensureActive()

        // Fetch from WhatsApp
        statusFiles.addAll(getStatusesFromApp(whatsAppPaths))

        // Check if coroutine is still active before proceeding
        ensureActive()

        // Fetch from WhatsApp Business
        statusFiles.addAll(getStatusesFromApp(whatsAppBusinessPaths))

        // Sort by last modified time (newest first)
        statusFiles.sortedByDescending { File(it.path).lastModified() }
    }

    private fun getStatusesFromApp(paths: List<String>): List<StatusItem> {
        val statusFiles = mutableListOf<StatusItem>()
        val externalStoragePath = Environment.getExternalStorageDirectory().absolutePath

        for (path in paths) {
            val fullPath = externalStoragePath + path
            val statusDir = File(fullPath)

            if (statusDir.exists() && statusDir.isDirectory) {
                try {
                    val files = statusDir.listFiles { file ->
                        file.isFile && isValidStatusFile(file.name)
                    }

                    files?.forEach { file ->
                        val statusItem = createStatusItem(file)
                        statusFiles.add(statusItem)
                    }
                } catch (e: Exception) {
                    // Log error but continue with other paths
                    e.printStackTrace()
                }
                break // If we found this path, no need to check others
            }
        }

        return statusFiles
    }

    private fun isValidStatusFile(fileName: String): Boolean {
        val lowerFileName = fileName.lowercase()
        return imageExtensions.any { lowerFileName.endsWith(it) } ||
                videoExtensions.any { lowerFileName.endsWith(it) }
    }

    private fun createStatusItem(file: File): StatusItem {
        val fileName = file.name.lowercase()
        val type = when {
            imageExtensions.any { fileName.endsWith(it) } -> MediaType.IMAGE
            videoExtensions.any { fileName.endsWith(it) } -> MediaType.VIDEO
            else -> MediaType.IMAGE // Default fallback
        }

        val timestamp = SimpleDateFormat("h:mm a", Locale.getDefault())
            .format(Date(file.lastModified()))

        return StatusItem(
            path = file.absolutePath,
            timestamp = timestamp,
            type = type
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel any ongoing loading job
        loadingJob?.cancel()
    }

    // Method to clear loading job - called from UI cleanup
    fun clearLoadingJob() {
        loadingJob?.cancel()
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(val statusItems: List<StatusItem>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

data class StatusItem(
    val path: String,
    val timestamp: String,
    val type: MediaType
)