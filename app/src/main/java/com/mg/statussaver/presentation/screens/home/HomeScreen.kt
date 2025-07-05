package com.mg.statussaver.presentation.screens.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mg.statussaver.utils.PermissionUtils

val TealGreen = Color(0xFF00A884)

enum class MediaType {
    IMAGE, VIDEO
}

@Composable
fun CategoryCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconBackground: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackground)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

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

@Composable
fun PermissionRationaleDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("This app requires storage permission to function properly. Please grant the permission.") },
        confirmButton = {
            TextButton(
                onClick = onRequestPermission
            ) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PermissionDeniedDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Denied") },
        text = { Text("Storage permission was denied. Please enable it in app settings.") },
        confirmButton = {
            TextButton(
                onClick = onOpenSettings
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory()),
    onStatusClick: (StatusItem) -> Unit = {},
    onDirectChatClick: () -> Unit = {},
    onNavigateToSaved: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val tabs = listOf("Recent", "Images", "Videos")

    // Permission states
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var hasInitialized by remember { mutableStateOf(false) }

    // Dynamic categories with actual counts
    val categories = remember(uiState) {
        when (val currentState = uiState) {
            is HomeUiState.Success -> {
                val items = currentState.statusItems
                listOf(
                    CategoryItemData("All Status", Icons.Outlined.Collections, items.size, Color(0xFF00B09C)),
                    CategoryItemData("Images", Icons.Outlined.Image, items.count { it.type == MediaType.IMAGE }, Color(0xFF4CAF50)),
                    CategoryItemData("Videos", Icons.Outlined.PlayCircle, items.count { it.type == MediaType.VIDEO }, Color(0xFF2196F3)),
                    CategoryItemData("Downloads", Icons.Outlined.Download, 0, Color(0xFFFF9800))
                )
            }
            else -> listOf(
                CategoryItemData("All Status", Icons.Outlined.Collections, 0, Color(0xFF00B09C)),
                CategoryItemData("Images", Icons.Outlined.Image, 0, Color(0xFF4CAF50)),
                CategoryItemData("Videos", Icons.Outlined.PlayCircle, 0, Color(0xFF2196F3)),
                CategoryItemData("Downloads", Icons.Outlined.Download, 0, Color(0xFFFF9800))
            )
        }
    }

    // Modern permission launcher for multiple permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permissions granted - load status files
            viewModel.loadStatusFiles()
        } else {
            // Some permissions denied - show appropriate dialog
            showPermissionDeniedDialog = true
        }
    }

    // Check permissions and load status files - only run once
    LaunchedEffect(hasInitialized) {
        if (!hasInitialized) {
            hasInitialized = true
            if (PermissionUtils.hasStoragePermissions(context)) {
                // Already have permissions - load files immediately
                viewModel.loadStatusFiles()
            } else {
                // Need to request permissions
                showPermissionRationale = true
            }
        }
    }

    // Re-check permissions when returning from settings
    LaunchedEffect(showPermissionDeniedDialog) {
        if (!showPermissionDeniedDialog && hasInitialized && PermissionUtils.hasStoragePermissions(context)) {
            viewModel.loadStatusFiles()
        }
    }

    // Cleanup on disposal
    DisposableEffect(Unit) {
        onDispose {
            // Clean up any pending operations
            viewModel.clearLoadingJob()
        }
    }

    // Permission dialogs
    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onRequestPermission = {
                showPermissionRationale = false
                val requiredPermissions = PermissionUtils.getRequiredStoragePermissions()
                permissionLauncher.launch(requiredPermissions)
            },
            onDismiss = {
                showPermissionRationale = false
            }
        )
    }

    if (showPermissionDeniedDialog) {
        PermissionDeniedDialog(
            onOpenSettings = {
                showPermissionDeniedDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            onDismiss = {
                showPermissionDeniedDialog = false
            }
        )
    }

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
                IconButton(onClick = onNavigateToSaved) {
                    Icon(
                        Icons.Outlined.Download,
                        contentDescription = "Saved",
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
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        ModernCategoryCard(
                            category = category,
                            onClick = {
                                // Handle category click based on type
                                when (category.title) {
                                    "Images" -> viewModel.setSelectedTab(1)
                                    "Videos" -> viewModel.setSelectedTab(2)
                                    "Downloads" -> viewModel.setSelectedTab(3)
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

            // Status Content based on selected tab and UI state
            item {
                when (uiState) {
                    is HomeUiState.Loading -> LoadingState()
                    is HomeUiState.Empty -> EmptyState()
                    is HomeUiState.Success -> {
                        val filteredItems = when (selectedTabIndex) {
                            1 -> (uiState as HomeUiState.Success).statusItems.filter { it.type == MediaType.IMAGE }
                            2 -> (uiState as HomeUiState.Success).statusItems.filter { it.type == MediaType.VIDEO }
                            3 -> emptyList() // Downloads - implement later
                            else -> (uiState as HomeUiState.Success).statusItems
                        }

                        if (filteredItems.isEmpty()) {
                            EmptyState()
                        } else {
                            StatusGrid(
                                items = filteredItems,
                                onStatusClick = onStatusClick
                            )
                        }
                    }
                    is HomeUiState.Error -> ErrorState((uiState as HomeUiState.Error).message)
                }
            }
        }
    }
}

// Data class for category items
data class CategoryItemData(
    val title: String,
    val icon: ImageVector,
    val count: Int,
    val color: Color
)

@Composable
fun DirectChatCard(onDirectChatClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDirectChatClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = TealGreen
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // WhatsApp Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Whatsapp,
                    contentDescription = "WhatsApp",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Open Direct Chat",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Chat without saving contacts",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }

            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Open",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ModernCategoryCard(
    category: CategoryItemData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    category.icon,
                    contentDescription = category.title,
                    tint = category.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = category.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatusGrid(
    items: List<StatusItem>,
    onStatusClick: (StatusItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(400.dp) // Give it a fixed height
    ) {
        items(items) { item ->
            StatusItemCard(item, onStatusClick)
        }
    }
}

@Composable
fun ErrorState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            Icons.Outlined.Error,
            contentDescription = "Error",
            tint = Color.Red,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Something went wrong",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
