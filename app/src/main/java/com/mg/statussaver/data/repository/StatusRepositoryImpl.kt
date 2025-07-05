package com.mg.statussaver.data.repository

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.mg.statussaver.domain.repository.StatusRepository
import com.mg.statussaver.presentation.screens.home.StatusItem
import com.mg.statussaver.presentation.screens.home.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StatusRepository {
    private val whatsappPaths = listOf(
        "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
        "/WhatsApp/Media/.Statuses"
    )

    private val whatsappBusinessPaths = listOf(
        "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
        "/WhatsApp Business/Media/.Statuses"
    )

    private val savedStatusPath = Environment.getExternalStorageDirectory().toString() + "/Status Saver"
    private val publicSavedPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Status Saver"
    private val appSpecificPath = Environment.getExternalStorageDirectory().toString() + "/Android/media/com.mg.statussaver/Status Saver"

    override suspend fun getWhatsAppStatuses(): List<StatusItem> = withContext(Dispatchers.IO) {
        val statusList = mutableListOf<StatusItem>()

        for (path in whatsappPaths) {
            val fullPath = Environment.getExternalStorageDirectory().toString() + path
            statusList.addAll(getStatusesFromFolder(fullPath))
        }

        statusList.sortedByDescending { it.timestamp }
    }

    override suspend fun getWhatsAppBusinessStatuses(): List<StatusItem> = withContext(Dispatchers.IO) {
        val statusList = mutableListOf<StatusItem>()

        for (path in whatsappBusinessPaths) {
            val fullPath = Environment.getExternalStorageDirectory().toString() + path
            statusList.addAll(getStatusesFromFolder(fullPath))
        }

        statusList.sortedByDescending { it.timestamp }
    }

    override suspend fun getDownloadedStatuses(): List<StatusItem> = withContext(Dispatchers.IO) {
        val publicFolderStatuses = getStatusesFromFolder(publicSavedPath)
        val appSpecificStatuses = getStatusesFromFolder(appSpecificPath)
        val legacyFolderStatuses = getStatusesFromFolder(savedStatusPath)

        // Combine all sources, remove duplicates by file path
        val allStatuses = (publicFolderStatuses + appSpecificStatuses + legacyFolderStatuses)
            .distinctBy { it.path }
            .sortedByDescending { it.timestamp }

        allStatuses
    }

    private fun getStatusesFromFolder(folderPath: String): List<StatusItem> {
        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) return emptyList()

        val files = folder.listFiles { file ->
            file.isFile && (file.name.endsWith(".jpg", ignoreCase = true) ||
                           file.name.endsWith(".jpeg", ignoreCase = true) ||
                           file.name.endsWith(".png", ignoreCase = true) ||
                           file.name.endsWith(".mp4", ignoreCase = true))
        } ?: return emptyList()

        return files.sortedByDescending { it.lastModified() }.map { file ->
            StatusItem(
                path = file.absolutePath,
                timestamp = getFormattedTime(file.lastModified()),
                type = if (file.name.endsWith(".mp4", ignoreCase = true)) MediaType.VIDEO else MediaType.IMAGE
            )
        }
    }

    override suspend fun downloadStatus(status: StatusItem): StatusRepository.DownloadResult = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(status.path)
            if (!srcFile.exists()) {
                return@withContext StatusRepository.DownloadResult(
                    success = false,
                    errorMessage = "Source file not found"
                )
            }

            // Create public directory in Pictures folder
            val publicDir = File(publicSavedPath)
            if (!publicDir.exists()) {
                val publicDirCreated = publicDir.mkdirs()
                if (!publicDirCreated) {
                    return@withContext StatusRepository.DownloadResult(
                        success = false,
                        errorMessage = "Failed to create Pictures/Status Saver directory"
                    )
                }
            }

            // Create app-specific directory
            val appSpecificDir = File(appSpecificPath)
            if (!appSpecificDir.exists()) {
                appSpecificDir.mkdirs() // Try to create, but don't fail if it doesn't work
            }

            // Generate unique filename if needed
            val baseFileName = status.path.substringAfterLast('/')
            val nameWithoutExt = baseFileName.substringBeforeLast('.')
            val ext = baseFileName.substringAfterLast('.')
            val timestamp = System.currentTimeMillis()
            val uniqueFileName = if (File(publicDir, baseFileName).exists()) {
                "${nameWithoutExt}_$timestamp.$ext"
            } else {
                baseFileName
            }

            // Save to public directory (primary location)
            val publicDestFile = File(publicDir, uniqueFileName)
            try {
                FileInputStream(srcFile).use { input ->
                    FileOutputStream(publicDestFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Also try to save to app-specific directory as backup
                try {
                    val appSpecificFile = File(appSpecificDir, uniqueFileName)
                    FileInputStream(srcFile).use { input ->
                        FileOutputStream(appSpecificFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    // Non-critical error - at least we saved to the public directory
                    e.printStackTrace()
                }

                // Notify media scanner to make the file appear in gallery
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(publicDestFile)
                context.sendBroadcast(mediaScanIntent)

                return@withContext StatusRepository.DownloadResult(
                    success = true,
                    savedPath = publicDestFile.absolutePath
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext StatusRepository.DownloadResult(
                    success = false,
                    errorMessage = "Failed to save file: ${e.message}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext StatusRepository.DownloadResult(
                success = false,
                errorMessage = "Unexpected error: ${e.message}"
            )
        }
    }

    override suspend fun deleteDownloadedStatus(status: StatusItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val fileName = status.path.substringAfterLast('/')
            var deleted = false

            // Try to delete from the primary location first
            val primaryFile = File(status.path)
            if (primaryFile.exists()) {
                deleted = primaryFile.delete() || deleted
            }

            // Try to delete from other possible locations
            val possibleLocations = listOf(publicSavedPath, appSpecificPath, savedStatusPath)
            for (location in possibleLocations) {
                val potentialFile = File(location, fileName)
                if (potentialFile.exists()) {
                    val result = potentialFile.delete()
                    deleted = deleted || result
                }
            }

            // Notify media scanner about deletion for gallery refresh
            try {
                val scanFileIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                scanFileIntent.data = Uri.fromFile(File(publicSavedPath)) // Scan directory
                context.sendBroadcast(scanFileIntent)
            } catch (e: Exception) {
                // Non-critical if this fails
                e.printStackTrace()
            }

            deleted
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun shareStatus(context: Context, status: StatusItem) = withContext(Dispatchers.Main) {
        try {
            val file = File(status.path)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (status.type == MediaType.IMAGE) "image/*" else "video/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Share Status"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFormattedTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    private fun getVideoDuration(file: File): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            retriever.release()

            duration?.let {
                val minutes = it / 60000
                val seconds = (it % 60000) / 1000
                String.format("%d:%02d", minutes, seconds)
            }
        } catch (e: Exception) {
            null
        }
    }
}
