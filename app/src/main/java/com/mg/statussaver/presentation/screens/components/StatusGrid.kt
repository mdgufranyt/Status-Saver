package com.mg.statussaver.presentation.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mg.statussaver.domain.model.Status_domain
import com.mg.statussaver.presentation.screens.home.MediaType
import com.mg.statussaver.presentation.screens.home.StatusItem

/**
 * A grid display of status items
 * This component delegates to the main StatusGrid implementation in HomeScreen.kt
 * to maintain consistency across the app
 */
@Composable
fun StatusGrid(
    statuses: List<Status_domain>,
    onDownloadClick: ((Status_domain) -> Unit)?,
    isSaved: Boolean = false
) {
    // Convert Status_domain to StatusItem
    val statusItems = statuses.map { status ->
        StatusItem(
            path = status.thumbnailUri,
            timestamp = status.timestamp,
            type = if (status.isVideo) MediaType.VIDEO else MediaType.IMAGE
        )
    }

    // Display the grid using LazyVerticalGrid
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(statusItems) { item ->
            // You can use your StatusItemCard or similar composable here
            // For demonstration, just show the path and type
            Column(
                modifier = Modifier
                    .background(Color.LightGray)
                    .padding(8.dp)
            ) {
                Text(text = item.path, maxLines = 1)
                Text(text = item.type.name)
            }
        }
    }
}