package com.mg.statussaver.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mg.statussaver.presentation.screens.permission.PermissionScreen

val TealGreen = Color(0xFF00A884)

@Composable
fun StatusItemCard(item: StatusItem, onClick: (StatusItem) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(item) }
    ) {
        // Load actual image using Coil
        AsyncImage(
            model = item.path,
            contentDescription = "Status",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Video play indicator
        if (item.type == MediaType.VIDEO) {
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

        // Timestamp
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(Color(0x88000000), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = item.timestamp,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(color = TealGreen)
    }
}

@Composable
fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            Icons.Outlined.FolderOff,
            contentDescription = "No Status",
            tint = Color.Gray,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Status Available",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "View a status in WhatsApp to see it here",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStatusClick: (StatusItem) -> Unit = {},
    onDirectChatClick: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},

    ) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()
    val statusCount by viewModel.statusCount.collectAsState()
    val tabs = listOf("Recent", "Images", "Videos")

    // Dynamic categories with real counts
    val categories = remember(statusCount) {
        listOf(
            CategoryItemData(
                "All Status",
                Icons.Outlined.Collections,
                statusCount.total,
                Color(0xFF00B09C)
            ),
            CategoryItemData("Images", Icons.Outlined.Image, statusCount.images, Color(0xFF4CAF50)),
            CategoryItemData(
                "Videos",
                Icons.Outlined.PlayCircle,
                statusCount.videos,
                Color(0xFF2196F3)
            ),
            CategoryItemData("Downloads", Icons.Outlined.Download, 0, Color(0xFFFF9800))
        )
    }

    // Show permission screen if permissions are required
    when (permissionState) {
        is PermissionState.Checking -> {
            LoadingState()
        }

        is PermissionState.Required -> {
            PermissionScreen(
                permissionManager = viewModel.permissionManager,
                onPermissionGranted = {
                    viewModel.onPermissionGranted()
                }
            )
        }

        is PermissionState.Granted -> {
            // Show main content when permissions are granted
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Modern Top App Bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Status Saver",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TealGreen
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToLanguage) {
                            Icon(
                                Icons.Rounded.Language,
                                contentDescription = "Language",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onNavigateToDownloads) {
                            Icon(
                                Icons.Outlined.Download,
                                contentDescription = "Downloads",
                                tint = Color.White
                            )
                        }
                    }
                )

                // Main Content with LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Direct Chat Card
                    item {
                        DirectChatCard(onDirectChatClick)
                    }

                    // Categories Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Refresh button
                            IconButton(
                                onClick = { viewModel.refreshStatuses() }
                            ) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categories) { category ->
                                ModernCategoryCard(
                                    category = category,
                                    isSelected = when (category.title) {
                                        "All Status" -> selectedTabIndex == 0
                                        "Images" -> selectedTabIndex == 1
                                        "Videos" -> selectedTabIndex == 2
                                        else -> false
                                    },
                                    onClick = {
                                        when (category.title) {
                                            "Images" -> viewModel.setSelectedTab(1)
                                            "Videos" -> viewModel.setSelectedTab(2)
                                            "Downloads" -> {
                                                // Navigate to SavedStatusScreen
                                                onNavigateToDownloads()
                                            }

                                            else -> viewModel.setSelectedTab(0)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Swipeable Tabs Section
                    item {
                        PrimaryTabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.Transparent,
                            contentColor = TealGreen
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { viewModel.setSelectedTab(index) },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Status Content
                    item {
                        StatusContent(
                            uiState = uiState,
                            onStatusClick = onStatusClick,
                            onRefresh = { viewModel.refreshStatuses() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DirectChatCard(onDirectChatClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDirectChatClick() },
        colors = CardDefaults.cardColors(
            containerColor = TealGreen
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Whatsapp,
                contentDescription = "WhatsApp",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Direct Chat",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Send message without saving number",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Arrow",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ModernCategoryCard(
    category: CategoryItemData,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = category.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                tint = category.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${category.count}",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatusContent(
    uiState: HomeUiState,
    onStatusClick: (StatusItem) -> Unit,
    onRefresh: () -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                CircularProgressIndicator(color = TealGreen)
            }
        }

        is HomeUiState.Empty -> {
            EmptyState()
        }

        is HomeUiState.Success -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(uiState.statusItems) { statusItem ->
                    StatusItemCard(
                        item = statusItem,
                        onClick = onStatusClick
                    )
                }
            }
        }

        is HomeUiState.Error -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Error: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "Retry",
                        tint = TealGreen
                    )
                }
            }
        }
    }
}

data class CategoryItemData(
    val title: String,
    val icon: ImageVector,
    val count: Int,
    val color: Color
)
