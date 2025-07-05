package com.mg.statussaver.domain.usecase

import com.mg.statussaver.domain.repository.StatusRepository
import com.mg.statussaver.presentation.screens.home.StatusItem
import javax.inject.Inject

class SaveStatusUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend operator fun invoke(statusItem: StatusItem): StatusRepository.DownloadResult =
        repository.downloadStatus(statusItem)
}