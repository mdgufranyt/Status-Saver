package com.mg.statussaver.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mg.statussaver.presentation.navigation.StatusSaverNavGraph
import com.mg.statussaver.ui.theme.StatusSaverTheme
import com.mg.statussaver.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions handled in the composable that launches this. */ }

    // Folder selection launcher
    private val folderSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Take persistable permission
            val takeFlags = intent.flags and (
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
            contentResolver.takePersistableUriPermission(uri, takeFlags)
            permissionManager.saveStatusFolderUri(uri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Google Mobile Ads SDK on a background thread.
//        MobileAds.initialize(this@MainActivity) {}              // Now it is implement into StatusSaverApplication, so we can remove this line


        setContent {
            StatusSaverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    StatusSaverNavGraph(navController)
                }
            }
        }
    }

    // Function to request storage permissions (can be called from UI components)
    fun requestStoragePermissions(permissions: Array<String>) {
        storagePermissionLauncher.launch(permissions)
    }

    // Function to launch folder selection
    fun openFolderSelection() {
        folderSelectionLauncher.launch(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Add any necessary cleanup logic here to prevent resource leaks
    }


}