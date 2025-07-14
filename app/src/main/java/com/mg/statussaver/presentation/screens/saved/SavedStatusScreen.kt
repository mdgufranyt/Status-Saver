package com.mg.statussaver.presentation.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mg.statussaver.presentation.screens.home.MediaType
import com.mg.statussaver.presentation.screens.home.StatusItem
import com.mg.statussaver.presentation.screens.home.VideoThumbnail
import com.mg.statussaver.presentation.screens.home.shareStatusFile
import com.mg.statussaver.ui.theme.TealPrimary
import com.mg.statussaver.utils.BannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedStatusScreen(navController: NavController) {
    val viewModel: SavedViewModel = hiltViewModel()
    val filteredStatuses by viewModel.filteredStatuses.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val tabs = listOf("All", "Images", "Videos")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Saved Statuses",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            ),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refreshStatuses() }) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        )

        // Tabs Section
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = TealPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { viewModel.setSelectedTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) TealPrimary else Color.Gray
                        )
                    }
                )
            }
        }

        // Content Section
        Box(modifier = Modifier
            .fillMaxSize()
            .weight(1f)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TealPrimary)
                    }
                }

                filteredStatuses.isEmpty() -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "No saved statuses",
                                tint = Color.Gray,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Saved Statuses",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (selectedTabIndex) {
                                    1 -> "No saved images found"
                                    2 -> "No saved videos found"
                                    else -> "Save some statuses to see them here"
                                },
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    // Status grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredStatuses) { statusItem ->
                            SavedStatusCard(
                                statusItem = statusItem,
                                onClick = {
                                    // Handle status click (e.g., open full screen view)
                                },
                                onDeleteClick = {
                                    viewModel.deleteStatus(statusItem.path)
                                }
                            )
                        }
                    }
                }
            }
        }
        BannerAdView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun SavedStatusCard(
    statusItem: StatusItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Content based on media type
            if (statusItem.type == MediaType.VIDEO) {
                VideoThumbnail(
                    videoPath = statusItem.path,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = statusItem.path,
                    contentDescription = "Saved Status",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Video play indicator
            if (statusItem.type == MediaType.VIDEO) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .background(Color(0x88000000), CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Share button - Bottom Left
            IconButton(
                onClick = { shareStatusFile(context, statusItem.path, statusItem.type) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color(0x88000000), CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete button - Bottom Right
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color(0x88000000), CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}