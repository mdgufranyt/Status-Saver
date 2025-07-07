package com.mg.statussaver.presentation.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mg.statussaver.presentation.screens.home.StatusItem
import com.mg.statussaver.domain.usecase.GetStatusesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val getStatuses: GetStatusesUseCase
) : ViewModel() {

    private val _savedStatuses = MutableStateFlow<List<StatusItem>>(emptyList())
    val savedStatuses: StateFlow<List<StatusItem>> = _savedStatuses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadStatuses() }

    fun loadStatuses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _savedStatuses.value = getStatuses.invoke(saved = true)
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
                }

                // Update the UI state
                _savedStatuses.value = _savedStatuses.value.filter { it.path != path }
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
                // Delete all files
                paths.forEach { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                // Update the UI state
                _savedStatuses.value = _savedStatuses.value.filter { it.path !in paths }
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