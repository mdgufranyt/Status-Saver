package com.mg.statussaver.presentation.screens.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mg.statussaver.R
import com.mg.statussaver.data.repository.StatusRepository
import com.mg.statussaver.domain.usecase.SaveStatusUseCase
import com.mg.statussaver.utils.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val permissionManager: PermissionManager,
    private val statusRepository: StatusRepository,
    private val saveStatusUseCase: SaveStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Checking)
    val permissionState = _permissionState.asStateFlow()

    private val _statusCount = MutableStateFlow(com.mg.statussaver.data.repository.StatusCount(0, 0, 0))
    val statusCount = _statusCount.asStateFlow()



    // for StatusViewerScreen---------------------------------------------------------

    private val _viewerItems = MutableStateFlow<List<StatusItem>>(emptyList())
    val viewerItems: StateFlow<List<StatusItem>> = _viewerItems.asStateFlow()

    private val _initialViewerIndex = MutableStateFlow(0)
    val initialViewerIndex: StateFlow<Int> = _initialViewerIndex.asStateFlow()

    fun openViewer(tabIndex: Int, selectedItem: StatusItem, allItems: List<StatusItem>) {
        _selectedTabIndex.value = tabIndex
        _viewerItems.value = allItems
        _initialViewerIndex.value = allItems.indexOfFirst { it.path == selectedItem.path }

    }

    // ------------------------------------------------------------------------------------



    init {
        checkPermissions()
    }

    fun setSelectedTab(index: Int) {
        _selectedTabIndex.value = index
        if (_permissionState.value == PermissionState.Granted) {
            loadStatusFiles()
        }
    }

    private fun checkPermissions() {
        viewModelScope.launch {
            _permissionState.value = PermissionState.Checking

            val hasStoragePermission = permissionManager.hasStoragePermission()
            val hasFolderAccess = permissionManager.hasStatusFolderAccess()

            _permissionState.value = when {
                hasStoragePermission && hasFolderAccess -> {
                    loadStatusFiles()
                    PermissionState.Granted
                }
                else -> PermissionState.Required
            }
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            _permissionState.value = PermissionState.Granted
            loadStatusFiles()
        }
    }

    fun loadStatusFiles() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Load status count for categories
                val count = statusRepository.getStatusCount()
                _statusCount.value = count

                // Load status files based on selected tab
                val statusItems = when (_selectedTabIndex.value) {
                    0 -> statusRepository.getStatusFiles() // All/Recent
                    1 -> statusRepository.getImageStatuses() // Images
                    2 -> statusRepository.getVideoStatuses() // Videos
                    else -> statusRepository.getStatusFiles()
                }

                _uiState.value = if (statusItems.isEmpty()) {
                    HomeUiState.Empty
                } else {
                    HomeUiState.Success(statusItems)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load status files")
            }
        }
    }

    fun refreshStatuses() {
        if (_permissionState.value == PermissionState.Granted) {
            loadStatusFiles()
        } else {
            checkPermissions()
        }
    }

    fun refreshPermissions() {
        checkPermissions()
    }

    /**
     * Downloads a status item to the device storage
     * Location: /storage/emulated/0/Download/StatusSaver
     */
    fun downloadStatus(statusItem: StatusItem, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = saveStatusUseCase(statusItem)
                onResult(result.success, result.errorMessage)
            } catch (e: Exception) {
                onResult(false, "Download failed: ${e.message}")
            }
        }
    }



}



sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Empty : HomeUiState()
    data class Success(val statusItems: List<StatusItem>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class PermissionState {
    data object Checking : PermissionState()
    data object Required : PermissionState()
    data object Granted : PermissionState()
}

data class StatusItem(
    val path: String,
    val timestamp: String,
    val type: MediaType
)

enum class MediaType {
    IMAGE,
    VIDEO
}