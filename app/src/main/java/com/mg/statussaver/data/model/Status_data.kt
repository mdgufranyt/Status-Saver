package com.mg.statussaver.data.model

data class Status_data(
    val id: String,
    val thumbnailUri: String,
    val isVideo: Boolean,
    val duration: String? = null,
    val timestamp: String = "",
    val isDownloaded: Boolean = false,
    val filePath: String? = null
)