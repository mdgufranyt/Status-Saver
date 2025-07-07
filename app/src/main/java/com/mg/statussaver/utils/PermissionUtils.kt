package com.mg.statussaver.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility class for handling storage permissions across different Android versions
 */
object PermissionUtils {

    /**
     * Check if the app has storage permissions
     */
    fun hasStoragePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Check for media permissions
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Check for external storage access
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 11 - Check for read and write external storage
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get the required storage permissions based on Android version
     */
    fun getRequiredStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Request media permissions
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Request external storage access
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            // Below Android 11 - Request read and write external storage
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * Check if a specific permission is granted
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if any of the required permissions are permanently denied
     */
    fun arePermissionsPermanentlyDenied(context: Context, permissions: Array<String>): Boolean {
        // This would require additional activity context to check shouldShowRequestPermissionRationale
        // For now, we'll return false and handle this in the UI layer
        return false
    }

    /**
     * Check if we should show permission rationale for any of the required permissions
     */
    fun shouldShowPermissionRationale(activity: androidx.activity.ComponentActivity): Boolean {
        val requiredPermissions = getRequiredStoragePermissions()
        return requiredPermissions.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }
    }

    /**
     * Get user-friendly permission description based on Android version
     */
    fun getPermissionDescription(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            "This app needs access to your photos and videos to find and save WhatsApp statuses."
        } else {
            "This app needs storage access to find WhatsApp statuses and save them to your device."
        }
    }

    /**
     * Get permission denial message
     */
    fun getPermissionDeniedMessage(): String {
        return "Storage permission is required to access WhatsApp statuses. Please grant permission in Settings."
    }

    /**
     * Check if we should show rationale for storage permissions
     */
    fun shouldShowPermissionRationale(context: Context): Boolean {
        val requiredPermissions = getRequiredStoragePermissions()
        return requiredPermissions.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED
        }
    }

    /**
     * Check if app has all file access (for Android 11+)
     */
    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            true // Not needed for older versions
        }
    }
}
