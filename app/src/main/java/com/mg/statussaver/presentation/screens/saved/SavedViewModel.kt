package com.mg.statussaver.presentation.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mg.statussaver.presentation.screens.home.StatusItem
import com.mg.statussaver.domain.usecase.GetStatusesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val getStatuses: GetStatusesUseCase
) : ViewModel() {

    private val _savedStatuses = MutableStateFlow<List<StatusItem>>(emptyList())
    val savedStatuses: StateFlow<List<StatusItem>> = _savedStatuses

    init { loadStatuses() }

    fun loadStatuses() {
        viewModelScope.launch {
            _savedStatuses.value = getStatuses.invoke(saved = true)
        }
    }
}