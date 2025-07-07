package com.mg.statussaver.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mg.statussaver.data.repository.StatusRepository
import com.mg.statussaver.utils.PermissionManager

class HomeViewModelFactory(
    private val permissionManager: PermissionManager,
    private val statusRepository: StatusRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(permissionManager, statusRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
