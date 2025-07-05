package com.mg.statussaver.utils

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

@Composable
fun RequestStoragePermission(
    onGranted: () -> Unit
) {
    val grantedState = remember { mutableStateOf(false) }
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else Manifest.permission.READ_EXTERNAL_STORAGE

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            grantedState.value = true
            onGranted()
        }
    }
    LaunchedEffect(Unit) {
        if (!grantedState.value) {
            launcher.launch(permission)
        }
    }
}