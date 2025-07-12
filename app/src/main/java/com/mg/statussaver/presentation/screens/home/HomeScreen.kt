package com.mg.statussaver.presentation.screens.home


import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Videocam
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mg.statussaver.presentation.screens.permission.PermissionScreen
import com.mg.statussaver.utils.BannerAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


val TealGreen = Color(0xFF00A884)

/**
 * Shares a status file (image or video) using Android's share intent
 */
fun shareStatusFile(context: android.content.Context, filePath: String, mediaType: MediaType) {
    try {
        val uri = if (filePath.startsWith("content://")) {
            // If it's already a content URI, use it directly
            filePath.toUri()
        } else {
            // If it's a file path, convert to FileProvider URI
            val file = File(filePath)
            if (!file.exists()) {
                android.util.Log.e("ShareStatus", "File does not exist: $filePath")
                return
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }

        val mimeType = when (mediaType) {
            MediaType.IMAGE -> "image/*"
            MediaType.VIDEO -> "video/*"
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Shared from Status Saver")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Status")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        android.util.Log.d("ShareStatus", "Sharing file: $filePath with URI: $uri")
        context.startActivity(chooserIntent)

    } catch (e: Exception) {
        android.util.Log.e("ShareStatus", "Error sharing file: $filePath", e)
        // You could show a toast here to inform the user
        android.widget.Toast.makeText(
            context,
            "Unable to share file",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun StatusItemCard(
    item: StatusItem,
    onClick: (StatusItem) -> Unit,
    onDownloadClick: (StatusItem) -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(item) }
    ) {
        if (item.type == MediaType.VIDEO) {
            // For videos, create a custom thumbnail
            VideoThumbnail(
                videoPath = item.path,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // For images, use AsyncImage directly
            AsyncImage(
                model = item.path,
                contentDescription = "Image Status",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

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

        // Share button (bottom-left, replacing timestamp)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            IconButton(
                onClick = { shareStatusFile(context, item.path, item.type) },
                modifier = Modifier
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
        }

        // Download button (bottom-right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            IconButton(
                onClick = { onDownloadClick(item) },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0x88000000), CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Download,
                    contentDescription = "Download",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
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
            fontSize = 21.sp,
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
    val context = LocalContext.current // Move context to proper Composable scope

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
//            CategoryItemData("Downloads", Icons.Outlined.Download, 0, Color(0xFFFF9800))
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

        is PermissionState.Granted -> Scaffold(
            topBar = {
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
                            Icon(Icons.Rounded.Language, contentDescription = "Language", tint = Color.White)
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                        IconButton(onClick = onNavigateToDownloads) {
                            Icon(Icons.Outlined.Download, contentDescription = "Downloads", tint = Color.White)
                        }
                    }
                )
            },
            bottomBar = {
                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            },
            content = { padding: PaddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        DirectChatCard(onDirectChatClick)
                    }
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
                            IconButton(onClick = { viewModel.refreshStatuses() }) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Color.White)
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(categories) { category ->
                                ModernCategoryCard(
                                    category = category,
                                    onClick = {
                                        when (category.title) {
                                            "Images" -> viewModel.setSelectedTab(1)
                                            "Videos" -> viewModel.setSelectedTab(2)
                                            else -> viewModel.setSelectedTab(0)
                                        }
                                    }
                                )
                            }
                        }
                    }
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
                    item {
                        StatusContent(
                            uiState = uiState,
                            onStatusClick = onStatusClick,
                            onRefresh = { viewModel.refreshStatuses() },
                            onDownloadClick = { statusItem ->
                                viewModel.downloadStatus(statusItem) { success, errorMessage ->
                                    val message = if (success) {
                                        "Status downloaded"
                                    } else {
                                        errorMessage ?: "Download failed"
                                    }
                                    android.widget.Toast.makeText(
                                        context,
                                        message,
                                        if (success) android.widget.Toast.LENGTH_SHORT else android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        )

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
    onClick: () -> Unit
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
    onRefresh: () -> Unit,
    onDownloadClick: (StatusItem) -> Unit = {}
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
                        onClick = onStatusClick,
                        onDownloadClick = onDownloadClick
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

@Composable
fun VideoThumbnail(
    videoPath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var thumbnail by remember(videoPath) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(videoPath) { mutableStateOf(true) }
    var hasError by remember(videoPath) { mutableStateOf(false) }

    // Simple memory cache to avoid repeated processing
    val thumbnailCache = remember { mutableMapOf<String, Bitmap?>() }

    LaunchedEffect(videoPath) {
        // Check cache first
        thumbnailCache[videoPath]?.let { cachedBitmap ->
            thumbnail = cachedBitmap
            isLoading = false
            hasError = false
            return@LaunchedEffect
        }

        isLoading = true
        hasError = false

        try {
            val bitmap = withContext(Dispatchers.IO) {
                var retriever: MediaMetadataRetriever? = null
                try {
                    retriever = MediaMetadataRetriever()

                    if (videoPath.startsWith("content://")) {
                        val uri = videoPath.toUri()
                        retriever.setDataSource(context, uri)
                    } else {
                        // Handle file paths
                        val file = File(videoPath)
                        if (file.exists() && file.canRead()) {
                            retriever.setDataSource(videoPath)
                        } else {
                            return@withContext null
                        }
                    }

                    // Get frame at 1 second (or first frame if video is shorter)
                    val timeUs = 1000000L // 1 second in microseconds
                    var frame =
                        retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

                    // If no frame at 1 second, try first frame
                    if (frame == null) {
                        frame =
                            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    }

                    // Resize bitmap to prevent memory issues
                    frame?.let { originalBitmap ->
                        val maxSize = 200
                        if (originalBitmap.width > maxSize || originalBitmap.height > maxSize) {
                            val ratio = minOf(
                                maxSize.toFloat() / originalBitmap.width,
                                maxSize.toFloat() / originalBitmap.height
                            )
                            val newWidth = (originalBitmap.width * ratio).toInt()
                            val newHeight = (originalBitmap.height * ratio).toInt()

                            val resized = originalBitmap.scale(newWidth, newHeight)
                            if (resized != originalBitmap) {
                                originalBitmap.recycle()
                            }
                            resized
                        } else {
                            originalBitmap
                        }
                    }
                } catch (e: Exception) {
                    null
                } finally {
                    try {
                        retriever?.release()
                    } catch (e: Exception) {
                        // Ignore cleanup errors
                    }
                }
            }

            // Cache the result (including null for failed attempts)
            thumbnailCache[videoPath] = bitmap
            thumbnail = bitmap
            hasError = bitmap == null

        } catch (e: Exception) {
            hasError = true
            thumbnail = null
        } finally {
            isLoading = false
        }
    }

    // Clean up cache when composable is removed
    DisposableEffect(videoPath) {
        onDispose {
            // Clean up old cache entries to prevent memory leaks
            if (thumbnailCache.size > 50) {
                val keysToRemove = thumbnailCache.keys.take(thumbnailCache.size - 40)
                keysToRemove.forEach { key ->
                    thumbnailCache[key]?.recycle()
                    thumbnailCache.remove(key)
                }
            }
        }
    }

    Box(modifier = modifier) {
        when {
            isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            thumbnail != null -> {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play icon overlay
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Filled.PlayCircleOutline,
                        contentDescription = "Play Video",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(32.dp)
                            .shadow(4.dp, CircleShape)
                    )
                }
            }

            else -> {
                // Error state - show default video icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Outlined.Videocam,
                        contentDescription = "Video",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
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