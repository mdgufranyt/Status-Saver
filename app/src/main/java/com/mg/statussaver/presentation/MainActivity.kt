package com.mg.statussaver.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mg.statussaver.presentation.navigation.StatusSaverNavGraph
import com.mg.statussaver.ui.theme.StatusSaverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Modern Activity Result API for storage permissions
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions handled in the composable that launches this. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onDestroy() {
        super.onDestroy()
        // Add any necessary cleanup logic here to prevent resource leaks
    }
}