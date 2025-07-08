package com.mg.statussaver.domain.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadEventManager @Inject constructor() {

    private val _downloadEvents = MutableSharedFlow<DownloadEvent>()
    val downloadEvents: SharedFlow<DownloadEvent> = _downloadEvents.asSharedFlow()

    suspend fun emitDownloadComplete(savedPath: String) {
        _downloadEvents.emit(DownloadEvent.DownloadComplete(savedPath))
    }

    suspend fun emitStatusDeleted() {
        _downloadEvents.emit(DownloadEvent.StatusDeleted)
    }
}

sealed class DownloadEvent {
    data class DownloadComplete(val savedPath: String) : DownloadEvent()
    object StatusDeleted : DownloadEvent()
}
