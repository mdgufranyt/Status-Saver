package com.mg.statussaver.presentation.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mg.statussaver.data.repository.StatusRepository
import com.mg.statussaver.domain.usecase.SaveStatusUseCase
import com.mg.statussaver.utils.PermissionManager

class HomeViewModelFactory(
    private val permissionManager: PermissionManager,
    private val statusRepository: StatusRepository,
    private val saveStatusUseCase: SaveStatusUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(permissionManager, statusRepository, saveStatusUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

