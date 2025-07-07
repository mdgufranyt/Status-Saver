package com.mg.statussaver.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mg.statussaver.presentation.screens.home.MediaType
import com.mg.statussaver.presentation.screens.home.StatusItem
import com.mg.statussaver.utils.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    suspend fun getStatusFiles(): List<StatusItem> = withContext(Dispatchers.IO) {
        val statusItems = mutableListOf<StatusItem>()

        try {
            val folderUri = permissionManager.getStatusFolderUri()
            if (folderUri != null) {
                val documentsTree = DocumentFile.fromTreeUri(context, folderUri)
                if (documentsTree != null && documentsTree.canRead()) {
                    val files = documentsTree.listFiles()

                    for (file in files) {
                        if (file.isFile && file.canRead()) {
                            val mimeType = file.type
                            val name = file.name ?: continue

                            // Skip temporary files and thumbnails
                            if (name.startsWith(".") || name.contains("nomedia")) {
                                continue
                            }

                            val mediaType = when {
                                mimeType?.startsWith("image/") == true -> MediaType.IMAGE
                                mimeType?.startsWith("video/") == true -> MediaType.VIDEO
                                name.endsWith(".jpg", true) ||
                                name.endsWith(".jpeg", true) ||
                                name.endsWith(".png", true) ||
                                name.endsWith(".webp", true) -> MediaType.IMAGE
                                name.endsWith(".mp4", true) ||
                                name.endsWith(".3gp", true) ||
                                name.endsWith(".mkv", true) -> MediaType.VIDEO
                                else -> continue
                            }

                            val timestamp = file.lastModified()
                            val formattedDate = if (timestamp > 0) {
                                dateFormat.format(Date(timestamp))
                            } else {
                                "Unknown"
                            }

                            statusItems.add(
                                StatusItem(
                                    path = file.uri.toString(),
                                    timestamp = formattedDate,
                                    type = mediaType
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort by timestamp (newest first)
        statusItems.sortedByDescending {
            try {
                dateFormat.parse(it.timestamp)?.time ?: 0
            } catch (e: Exception) {
                0
            }
        }
    }

    suspend fun getImageStatuses(): List<StatusItem> = withContext(Dispatchers.IO) {
        getStatusFiles().filter { it.type == MediaType.IMAGE }
    }

    suspend fun getVideoStatuses(): List<StatusItem> = withContext(Dispatchers.IO) {
        getStatusFiles().filter { it.type == MediaType.VIDEO }
    }

    suspend fun getStatusCount(): StatusCount = withContext(Dispatchers.IO) {
        val allStatuses = getStatusFiles()
        val imageCount = allStatuses.count { it.type == MediaType.IMAGE }
        val videoCount = allStatuses.count { it.type == MediaType.VIDEO }

        StatusCount(
            total = allStatuses.size,
            images = imageCount,
            videos = videoCount
        )
    }
}

data class StatusCount(
    val total: Int,
    val images: Int,
    val videos: Int
)
