package com.mg.statussaver.presentation.screens.saved

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun SavedStatusScreen(_navController: NavController) {
    val viewModel: SavedViewModel = hiltViewModel()
    val savedStatuses by viewModel.savedStatuses.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Saved Statuses",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        // Use the StatusGrid from the components package
        com.mg.statussaver.presentation.screens.components.StatusGrid(
            statuses = savedStatuses.map {
                com.mg.statussaver.domain.model.Status_domain(
                    id = it.path,
                    thumbnailUri = it.path,
                    isVideo = it.type == com.mg.statussaver.presentation.screens.home.MediaType.VIDEO,
                    duration = null,
                    timestamp = it.timestamp,
                    isDownloaded = false,
                    filePath = it.path
                )
            },
            onDownloadClick = null, // Already downloaded
            isSaved = true
        )
    }
}