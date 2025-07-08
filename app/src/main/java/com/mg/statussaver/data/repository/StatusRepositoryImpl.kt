package com.mg.statussaver.data.repository

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.mg.statussaver.domain.repository.StatusRepository
import com.mg.statussaver.presentation.screens.home.MediaType
import com.mg.statussaver.presentation.screens.home.StatusItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
    private val publicSavedPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/StatusSaver"
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
            android.util.Log.d("DownloadStatus", "Attempting to download: ${status.path}")

            // Check if this is a content URI or file path
            if (status.path.startsWith("content://")) {
                android.util.Log.d("DownloadStatus", "Handling content URI")
                return@withContext handleContentUriDownload(status)
            } else {
                android.util.Log.d("DownloadStatus", "Handling file path")
                return@withContext handleFilePathDownload(status)
            }

        } catch (e: Exception) {
            android.util.Log.e("DownloadStatus", "Download exception", e)
            e.printStackTrace()
            return@withContext StatusRepository.DownloadResult(
                success = false,
                errorMessage = "Download failed: ${e.message}"
            )
        }
    }

    private suspend fun handleContentUriDownload(status: StatusItem): StatusRepository.DownloadResult = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(status.path)
            android.util.Log.d("DownloadStatus", "Parsed URI: $uri")

            // Extract filename from the URI
            val fileName = extractFileNameFromUri(uri) ?: run {
                android.util.Log.e("DownloadStatus", "Could not extract filename from URI")
                return@withContext StatusRepository.DownloadResult(
                    success = false,
                    errorMessage = "Could not determine filename from URI"
                )
            }

            android.util.Log.d("DownloadStatus", "Extracted filename: $fileName")

            // Create download directory
            val downloadDir = File(publicSavedPath)
            if (!downloadDir.exists()) {
                val created = downloadDir.mkdirs()
                if (!created) {
                    return@withContext StatusRepository.DownloadResult(
                        success = false,
                        errorMessage = "Failed to create download directory"
                    )
                }
            }

            // Generate unique filename if needed
            val nameWithoutExt = fileName.substringBeforeLast('.')
            val ext = fileName.substringAfterLast('.')
            val timestamp = System.currentTimeMillis()
            val uniqueFileName = if (File(downloadDir, fileName).exists()) {
                "${nameWithoutExt}_$timestamp.$ext"
            } else {
                fileName
            }

            val destFile = File(downloadDir, uniqueFileName)

            // Copy content from URI to destination file
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: run {
                android.util.Log.e("DownloadStatus", "Could not open input stream from URI")
                return@withContext StatusRepository.DownloadResult(
                    success = false,
                    errorMessage = "Could not access source file"
                )
            }

            // Verify the copy was successful
            if (!destFile.exists() || destFile.length() == 0L) {
                android.util.Log.e("DownloadStatus", "File copy verification failed")
                return@withContext StatusRepository.DownloadResult(
                    success = false,
                    errorMessage = "File copy verification failed"
                )
            }

            android.util.Log.d("DownloadStatus", "Successfully copied file to: ${destFile.absolutePath}")

            // Notify media scanner
            try {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(destFile)
                context.sendBroadcast(mediaScanIntent)
            } catch (e: Exception) {
                android.util.Log.w("DownloadStatus", "Media scanner notification failed", e)
            }

            return@withContext StatusRepository.DownloadResult(
                success = true,
                savedPath = destFile.absolutePath
            )

        } catch (e: Exception) {
            android.util.Log.e("DownloadStatus", "Content URI download failed", e)
            return@withContext StatusRepository.DownloadResult(
                success = false,
                errorMessage = "Failed to download from content URI: ${e.message}"
            )
        }
    }

    private suspend fun handleFilePathDownload(status: StatusItem): StatusRepository.DownloadResult = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(status.path)

            android.util.Log.d("DownloadStatus", "File exists: ${srcFile.exists()}")
            android.util.Log.d("DownloadStatus", "File canRead: ${srcFile.canRead()}")
            android.util.Log.d("DownloadStatus", "File length: ${srcFile.length()}")

            // Enhanced file existence and accessibility check
            if (!srcFile.exists()) {
                android.util.Log.d("DownloadStatus", "Source file not found, searching alternatives...")
                // Try to find the file in alternative locations
                val fileName = status.path.substringAfterLast('/')
                android.util.Log.d("DownloadStatus", "Looking for filename: $fileName")
                val alternativeFile = findFileInAlternativeLocations(fileName)

                if (alternativeFile == null) {
                    android.util.Log.e("DownloadStatus", "No alternative file found")
                    return@withContext StatusRepository.DownloadResult(
                        success = false,
                        errorMessage = "Source file not found: ${status.path}"
                    )
                }

                android.util.Log.d("DownloadStatus", "Found alternative file: ${alternativeFile.absolutePath}")
                // Use the alternative file if found
                return@withContext copyFileToDestination(alternativeFile, fileName)
            }

            // Check if file is readable
            if (!srcFile.canRead()) {
                android.util.Log.e("DownloadStatus", "File is not readable")
                return@withContext StatusRepository.DownloadResult(
                    success = false,
                    errorMessage = "Source file is not readable: ${status.path}"
                )
            }

            val fileName = srcFile.name
            android.util.Log.d("DownloadStatus", "Proceeding with file copy for: $fileName")
            return@withContext copyFileToDestination(srcFile, fileName)

        } catch (e: Exception) {
            android.util.Log.e("DownloadStatus", "File path download failed", e)
            return@withContext StatusRepository.DownloadResult(
                success = false,
                errorMessage = "Failed to download file: ${e.message}"
            )
        }
    }

    private fun extractFileNameFromUri(uri: Uri): String? {
        return try {
            // Try to get the display name from the document URI
            val uriString = uri.toString()

            // For document URIs, extract the filename from the last segment
            if (uriString.contains("%2F")) {
                // URL decoded filename extraction
                val decoded = java.net.URLDecoder.decode(uriString, "UTF-8")
                val fileName = decoded.substringAfterLast("/")
                if (fileName.isNotEmpty() && fileName.contains(".")) {
                    return fileName
                }
            }

            // Fallback: extract from URI path
            uri.lastPathSegment?.let { segment ->
                if (segment.contains(".")) {
                    return segment
                }
            }

            // Last resort: generate a filename based on timestamp and type
            val timestamp = System.currentTimeMillis()
            return "status_$timestamp.mp4" // Default to mp4, adjust based on content type if needed

        } catch (e: Exception) {
            android.util.Log.e("DownloadStatus", "Error extracting filename from URI", e)
            null
        }
    }

    private suspend fun findFileInAlternativeLocations(fileName: String): File? = withContext(Dispatchers.IO) {
        val alternativePaths = listOf(
            // Try both WhatsApp and WhatsApp Business paths
            "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
            "/WhatsApp/Media/.Statuses",
            "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
            "/WhatsApp Business/Media/.Statuses"
        )

        for (path in alternativePaths) {
            val fullPath = Environment.getExternalStorageDirectory().toString() + path
            val folder = File(fullPath)
            if (folder.exists() && folder.isDirectory) {
                val file = File(folder, fileName)
                if (file.exists() && file.canRead()) {
                    return@withContext file
                }
            }
        }
        null
    }

    private suspend fun copyFileToDestination(srcFile: File, fileName: String): StatusRepository.DownloadResult = withContext(Dispatchers.IO) {
        try {
            // Create download directory
            val publicDir = File(publicSavedPath)
            if (!publicDir.exists()) {
                val publicDirCreated = publicDir.mkdirs()
                if (!publicDirCreated) {
                    return@withContext StatusRepository.DownloadResult(
                        success = false,
                        errorMessage = "Failed to create download directory: $publicSavedPath"
                    )
                }
            }

            // Generate unique filename if needed
            val nameWithoutExt = fileName.substringBeforeLast('.')
            val ext = fileName.substringAfterLast('.')
            val timestamp = System.currentTimeMillis()
            val uniqueFileName = if (File(publicDir, fileName).exists()) {
                "${nameWithoutExt}_$timestamp.$ext"
            } else {
                fileName
            }

            // Copy file to destination
            val destFile = File(publicDir, uniqueFileName)
            try {
                srcFile.copyTo(destFile, overwrite = false)

                // Verify the copy was successful
                if (!destFile.exists() || destFile.length() == 0L) {
                    return@withContext StatusRepository.DownloadResult(
                        success = false,
                        errorMessage = "File copy verification failed"
                    )
                }

                // Create app-specific backup copy (optional)
                try {
                    val appSpecificDir = File(appSpecificPath)
                    if (!appSpecificDir.exists()) {
                        appSpecificDir.mkdirs()
                    }
                    val backupFile = File(appSpecificDir, uniqueFileName)
                    srcFile.copyTo(backupFile, overwrite = false)
                } catch (e: Exception) {
                    // Non-critical error - main copy succeeded
                    e.printStackTrace()
                }

                // Notify media scanner
                try {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = Uri.fromFile(destFile)
                    context.sendBroadcast(mediaScanIntent)
                } catch (e: Exception) {
                    // Non-critical error
                    e.printStackTrace()
                }

                return@withContext StatusRepository.DownloadResult(
                    success = true,
                    savedPath = destFile.absolutePath
                )

            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext StatusRepository.DownloadResult(
                    success = false,
                    errorMessage = "Failed to copy file: ${e.message}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext StatusRepository.DownloadResult(
                success = false,
                errorMessage = "Unexpected error during copy: ${e.message}"
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