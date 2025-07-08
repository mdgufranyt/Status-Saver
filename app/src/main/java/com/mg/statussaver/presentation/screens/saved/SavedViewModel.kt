package com.mg.statussaver.presentation.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mg.statussaver.presentation.screens.home.StatusItem
import com.mg.statussaver.presentation.screens.home.MediaType
import com.mg.statussaver.domain.usecase.GetStatusesUseCase
import com.mg.statussaver.domain.events.DownloadEventManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val getStatuses: GetStatusesUseCase,
    private val downloadEventManager: DownloadEventManager
) : ViewModel() {

    private val _savedStatuses = MutableStateFlow<List<StatusItem>>(emptyList())
    val savedStatuses: StateFlow<List<StatusItem>> = _savedStatuses

    private val _filteredStatuses = MutableStateFlow<List<StatusItem>>(emptyList())
    val filteredStatuses: StateFlow<List<StatusItem>> = _filteredStatuses

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadStatuses()
        observeDownloadEvents()
    }

    fun setSelectedTab(index: Int) {
        _selectedTabIndex.value = index
        filterStatusesByTab(index)
    }

    private fun filterStatusesByTab(tabIndex: Int) {
        val allStatuses = _savedStatuses.value
        _filteredStatuses.value = when (tabIndex) {
            0 -> allStatuses // All
            1 -> allStatuses.filter { it.type == MediaType.IMAGE } // Images
            2 -> allStatuses.filter { it.type == MediaType.VIDEO } // Videos
            else -> allStatuses
        }
    }

    private fun observeDownloadEvents() {
        viewModelScope.launch {
            downloadEventManager.downloadEvents.collect { event ->
                when (event) {
                    is com.mg.statussaver.domain.events.DownloadEvent.DownloadComplete -> {
                        // Refresh the saved statuses list when a new download completes
                        loadStatuses()
                    }
                    is com.mg.statussaver.domain.events.DownloadEvent.StatusDeleted -> {
                        // Handle status deletion event (optional - we already handle this locally)
                        // This ensures consistency if deletions happen from other places
                    }
                }
            }
        }
    }

    fun loadStatuses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _savedStatuses.value = getStatuses.invoke(saved = true)
                // Apply the current tab filter to the loaded statuses
                filterStatusesByTab(_selectedTabIndex.value)
            } catch (e: Exception) {
                // Handle error - could emit to a separate error state
                _savedStatuses.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteStatus(path: String) {
        viewModelScope.launch {
            try {
                // Delete the actual file
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                    // Emit delete event to update downloads count in HomeViewModel
                    downloadEventManager.emitStatusDeleted()
                }

                // Update the UI state
                _savedStatuses.value = _savedStatuses.value.filter { it.path != path }
                // Refresh the filtered statuses as well
                _filteredStatuses.value = _filteredStatuses.value.filter { it.path != path }
            } catch (e: Exception) {
                // Handle error - could show a toast or error message
                // For now, just refresh the list to reflect actual state
                loadStatuses()
            }
        }
    }

    fun deleteMultipleStatuses(paths: List<String>) {
        viewModelScope.launch {
            try {
                var deletedCount = 0
                // Delete all files
                paths.forEach { path ->
                    val file = File(path)
                    if (file.exists()) {
                        if (file.delete()) {
                            deletedCount++
                        }
                    }
                }

                // Emit delete events for each successfully deleted file
                repeat(deletedCount) {
                    downloadEventManager.emitStatusDeleted()
                }

                // Update the UI state
                _savedStatuses.value = _savedStatuses.value.filter { it.path !in paths }
                // Refresh the filtered statuses as well
                _filteredStatuses.value = _filteredStatuses.value.filter { it.path !in paths }
            } catch (e: Exception) {
                // Handle error - refresh the list to reflect actual state
                loadStatuses()
            }
        }
    }

    fun refreshStatuses() {
        loadStatuses()
    }
}