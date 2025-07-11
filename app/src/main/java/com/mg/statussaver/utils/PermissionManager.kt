package com.mg.statussaver.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        const val STORAGE_PERMISSION_REQUEST_CODE = 1001
        const val FOLDER_SELECTION_REQUEST_CODE = 1002
        const val PREFS_NAME = "status_saver_prefs"
        const val KEY_STATUS_FOLDER_URI = "status_folder_uri"
        const val KEY_BUSINESS_STATUS_FOLDER_URI = "business_status_folder_uri"
        const val KEY_PERMISSION_GRANTED = "permission_granted"
        const val KEY_SELECTED_APP_TYPE = "selected_app_type"

        // App types
        const val APP_TYPE_WHATSAPP = "whatsapp"
        const val APP_TYPE_WHATSAPP_BUSINESS = "whatsapp_business"
    }

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, we prefer MANAGE_EXTERNAL_STORAGE
            Environment.isExternalStorageManager() || hasBasicStoragePermission()
        } else {
            // For Android 10 and below
            hasBasicStoragePermission()
        }
    }

    private fun hasBasicStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses media permissions
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11-12
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 10 and below
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivityForResult(intent, requestCode)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivityForResult(intent, requestCode)
            }
        } else {
            ActivityCompat.requestPermissions(
                activity,
                REQUIRED_PERMISSIONS,
                requestCode
            )
        }
    }

    fun hasStatusFolderAccess(): Boolean {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        val uriString = sharedPrefs.getString(KEY_STATUS_FOLDER_URI, null) ?: return false

        try {
            val uri = Uri.parse(uriString)
            val documentFile = DocumentFile.fromTreeUri(context, uri) ?: return false
            return documentFile.canRead()
        } catch (e: Exception) {
            return false
        }
    }

    fun getStatusFolderUri(): Uri? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        val uriString = sharedPrefs.getString(KEY_STATUS_FOLDER_URI, null) ?: return null
        return Uri.parse(uriString)
    }

    fun saveStatusFolderUri(uri: Uri) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_STATUS_FOLDER_URI, uri.toString()).apply()

        // Take persistable permission
        try {
            val contentResolver = context.contentResolver
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            // Log error or handle appropriately
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_PERMISSION_GRANTED, granted).apply()
    }

    fun isPermissionGranted(): Boolean {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        return sharedPrefs.getBoolean(KEY_PERMISSION_GRANTED, false)
    }

    fun createFolderSelectionIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        return intent
    }

    fun createWhatsAppStatusFolderIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

        // Try to set initial URI to WhatsApp status folder
        try {
            val whatsappStatusUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses")
            intent.putExtra("android.provider.extra.INITIAL_URI", whatsappStatusUri)
        } catch (e: Exception) {
            // If setting initial URI fails, fall back to regular folder picker
        }

        return intent
    }

    fun createWhatsAppBusinessStatusFolderIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

        // Try to set initial URI to WhatsApp Business status folder
        try {
            val whatsappBusinessStatusUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%2FMedia%2F.Statuses")
            intent.putExtra("android.provider.extra.INITIAL_URI", whatsappBusinessStatusUri)
        } catch (e: Exception) {
            // If setting initial URI fails, fall back to regular folder picker
        }

        return intent
    }

    fun shouldShowRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    // WhatsApp related utility methods
    fun isWhatsAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isWhatsAppBusinessInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // WhatsApp Business specific methods
    fun hasBusinessStatusFolderAccess(): Boolean {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        val uriString = sharedPrefs.getString(KEY_BUSINESS_STATUS_FOLDER_URI, null) ?: return false

        try {
            val uri = Uri.parse(uriString)
            val documentFile = DocumentFile.fromTreeUri(context, uri) ?: return false
            return documentFile.canRead()
        } catch (e: Exception) {
            return false
        }
    }

    fun getBusinessStatusFolderUri(): Uri? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        val uriString = sharedPrefs.getString(KEY_BUSINESS_STATUS_FOLDER_URI, null) ?: return null
        return Uri.parse(uriString)
    }

    fun saveBusinessStatusFolderUri(uri: Uri) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_BUSINESS_STATUS_FOLDER_URI, uri.toString()).apply()

        // Take persistable permission
        try {
            val contentResolver = context.contentResolver
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            // Log error or handle appropriately
        }
    }

    fun getSelectedAppType(): String {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_SELECTED_APP_TYPE, APP_TYPE_WHATSAPP) ?: APP_TYPE_WHATSAPP
    }

    fun setSelectedAppType(appType: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_SELECTED_APP_TYPE, appType).apply()
    }

    fun getAvailableApps(): List<String> {
        val availableApps = mutableListOf<String>()
        if (isWhatsAppInstalled()) {
            availableApps.add(APP_TYPE_WHATSAPP)
        }
        if (isWhatsAppBusinessInstalled()) {
            availableApps.add(APP_TYPE_WHATSAPP_BUSINESS)
        }
        return availableApps
    }

    fun createStatusFolderIntentForAppType(appType: String): Intent {
        return when (appType) {
            APP_TYPE_WHATSAPP_BUSINESS -> createWhatsAppBusinessStatusFolderIntent()
            else -> createWhatsAppStatusFolderIntent()
        }
    }

    fun saveStatusFolderUriForAppType(uri: Uri, appType: String) {
        when (appType) {
            APP_TYPE_WHATSAPP_BUSINESS -> saveBusinessStatusFolderUri(uri)
            else -> saveStatusFolderUri(uri)
        }
    }

    fun hasStatusFolderAccessForAppType(appType: String): Boolean {
        return when (appType) {
            APP_TYPE_WHATSAPP_BUSINESS -> hasBusinessStatusFolderAccess()
            else -> hasStatusFolderAccess()
        }
    }

    fun getStatusFolderUriForAppType(appType: String): Uri? {
        return when (appType) {
            APP_TYPE_WHATSAPP_BUSINESS -> getBusinessStatusFolderUri()
            else -> getStatusFolderUri()
        }
    }

    fun createDirectStatusFolderIntentForAppType(appType: String): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

        // Set the exact path for the status folder
        try {
            val statusFolderUri = when (appType) {
                APP_TYPE_WHATSAPP_BUSINESS -> {
                    // Direct path to WhatsApp Business statuses folder
                    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%2FMedia%2F.Statuses")
                }
                else -> {
                    // Direct path to WhatsApp statuses folder
                    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses")
                }
            }
            intent.putExtra("android.provider.extra.INITIAL_URI", statusFolderUri)
            intent.putExtra("android.provider.extra.SHOW_ADVANCED", true)
        } catch (e: Exception) {
            // If setting direct URI fails, fall back to media folder
            val mediaFolderUri = when (appType) {
                APP_TYPE_WHATSAPP_BUSINESS -> {
                    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%2FMedia")
                }
                else -> {
                    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia")
                }
            }
            intent.putExtra("android.provider.extra.INITIAL_URI", mediaFolderUri)
        }

        return intent
    }

    fun createAutoNavigateStatusFolderIntent(appType: String): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

        // Try multiple approaches to navigate to the correct folder
        try {
            // First approach: Direct to .Statuses folder
            val directStatusUri = when (appType) {
                APP_TYPE_WHATSAPP_BUSINESS -> {
                    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%2FMedia%2F.Statuses")
                }
                else -> {
                    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses")
                }
            }

            intent.putExtra("android.provider.extra.INITIAL_URI", directStatusUri)
            intent.putExtra("android.provider.extra.SHOW_ADVANCED", true)

            // Add extra data to help with navigation
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
            intent.putExtra("android.provider.extra.SHOW_FILESIZE", true)

        } catch (e: Exception) {
            // Fallback to Android/media folder
            val androidMediaUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia")
            intent.putExtra("android.provider.extra.INITIAL_URI", androidMediaUri)
        }

        return intent
    }

    // Helper method to validate if the selected folder is correct
    fun isCorrectStatusFolder(uri: Uri, appType: String): Boolean {
        try {
            val documentFile = DocumentFile.fromTreeUri(context, uri) ?: return false
            val uriPath = uri.toString()

            return when (appType) {
                APP_TYPE_WHATSAPP_BUSINESS -> {
                    uriPath.contains("com.whatsapp.w4b") &&
                    (uriPath.contains(".Statuses") || uriPath.contains("WhatsApp/Media"))
                }
                else -> {
                    uriPath.contains("com.whatsapp") &&
                    !uriPath.contains("com.whatsapp.w4b") &&
                    (uriPath.contains(".Statuses") || uriPath.contains("WhatsApp/Media"))
                }
            }
        } catch (e: Exception) {
            return false
        }
    }

    // Method to get the correct status folder path text for display
    fun getStatusFolderPathText(appType: String): String {
        return when (appType) {
            APP_TYPE_WHATSAPP_BUSINESS -> "Android/media/com.whatsapp.w4b/WhatsApp/Media/.Statuses"
            else -> "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
        }
    }
}
