package com.mg.statussaver.presentation.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mg.statussaver.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedStatusScreen(navController: NavController) {
    val viewModel: SavedViewModel = hiltViewModel()
    val savedStatuses by viewModel.savedStatuses.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStatusForDelete by remember { mutableStateOf<String?>(null) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = if (isSelectionMode) "${selectedItems.size} selected" else "Saved Statuses",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            ),
            navigationIcon = {
                IconButton(onClick = {
                    if (isSelectionMode) {
                        isSelectionMode = false
                        selectedItems = setOf()
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                if (isSelectionMode) {
                    // Delete selected items
                    IconButton(onClick = {
                        showDeleteDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Selected",
                            tint = Color.White
                        )
                    }

                    // Select all
                    IconButton(onClick = {
                        selectedItems = if (selectedItems.size == savedStatuses.size) {
                            setOf()
                        } else {
                            savedStatuses.map { it.path }.toSet()
                        }
                    }) {
                        Icon(
                            imageVector = if (selectedItems.size == savedStatuses.size)
                                Icons.Default.Delete else Icons.Default.SelectAll,
                            contentDescription = if (selectedItems.size == savedStatuses.size)
                                "Deselect All" else "Select All",
                            tint = Color.White
                        )
                    }
                }
            }
        )

        // Content
        when {
            savedStatuses.isEmpty() -> {
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
                            text = "Save some statuses to see them here",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(savedStatuses) { statusItem ->
                        SavedStatusCard(
                            statusItem = statusItem,
                            isSelected = selectedItems.contains(statusItem.path),
                            isSelectionMode = isSelectionMode,
                            onLongClick = {
                                isSelectionMode = true
                                selectedItems = selectedItems.plus(statusItem.path)
                            },
                            onClick = {
                                if (isSelectionMode) {
                                    selectedItems = if (selectedItems.contains(statusItem.path)) {
                                        selectedItems.minus(statusItem.path)
                                    } else {
                                        selectedItems.plus(statusItem.path)
                                    }
                                } else {
                                    // Open status viewer
                                    // TODO: Navigate to status viewer
                                }
                            },
                            onDeleteClick = {
                                selectedStatusForDelete = statusItem.path
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = if (selectedItems.isNotEmpty()) "Delete Selected Items?" else "Delete Status?",
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = if (selectedItems.isNotEmpty())
                        "Are you sure you want to delete ${selectedItems.size} selected items?"
                    else
                        "Are you sure you want to delete this status?",
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (selectedItems.isNotEmpty()) {
                                // Delete multiple items
                                viewModel.deleteMultipleStatuses(selectedItems.toList())
                                selectedItems = setOf()
                                isSelectionMode = false
                            } else {
                                // Delete single item
                                selectedStatusForDelete?.let { path ->
                                    viewModel.deleteStatus(path)
                                }
                            }
                            showDeleteDialog = false
                            selectedStatusForDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedStatusForDelete = null
                    }
                ) {
                    Text("Cancel", color = TealPrimary)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }
}

@Composable
private fun SavedStatusCard(
    statusItem: com.mg.statussaver.presentation.screens.home.StatusItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TealPrimary.copy(alpha = 0.3f) else Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Status preview
            AsyncImage(
                model = statusItem.path,
                contentDescription = "Status preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Video indicator
            if (statusItem.type == MediaType.VIDEO) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Selection indicator
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            if (isSelected) TealPrimary else Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                // Delete button (only visible when not in selection mode)
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Image/Video type indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (statusItem.type == MediaType.VIDEO)
                        Icons.Default.Videocam else Icons.Default.Image,
                    contentDescription = statusItem.type.name,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}