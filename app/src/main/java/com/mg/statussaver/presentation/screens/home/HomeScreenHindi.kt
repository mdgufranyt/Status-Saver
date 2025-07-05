package com.mg.statussaver.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mg.statussaver.R
import androidx.lifecycle.viewmodel.compose.viewModel

// Data class for categories specific to Hindi screen
data class CategoryItem(
    val title: String,
    val icon: ImageVector,
    val count: Int,
    val color: Color
)

/**
 * Hindi Home Screen for WhatsApp Status Saver App
 * Features modern UI with status grid, categories, and download management in Hindi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenHindi(navController: NavController? = null) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("हाल ही में", "तस्वीरें", "वीडियो", "डाउनलोड")

    // Sample data with Hindi labels - using available icons
    val categories = listOf(
        CategoryItem("सभी स्टेटस", Icons.Default.Done, 24, Color(0xFF00B09C)),
        CategoryItem("तस्वीरें", Icons.Default.Done, 18, Color(0xFF4CAF50)),
        CategoryItem("वीडियो", Icons.Default.Done, 6, Color(0xFF2196F3)),
        CategoryItem("डाउनलोड", Icons.Default.Done, 12, Color(0xFFFF9800))
    )

    // Remove dummy data - now using real ViewModel
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Convert real status items for the grid
    val statusItems = when (val currentState = uiState) {
        is HomeUiState.Success -> currentState.statusItems
        else -> emptyList()
    }

    // Load status files on composition
    LaunchedEffect(Unit) {
        viewModel.loadStatusFiles()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "स्टेटस सेवर",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF00B09C)
            ),
            actions = {
                IconButton(onClick = {
                    // Navigate to Language Selection Screen
                    navController?.navigate("language")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.language),
                        contentDescription = "भाषा",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { /* Settings */ }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "सेटिंग्स",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "और",
                        tint = Color.White
                    )
                }
            }
        )

        // Main Content
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // WhatsApp Status Info Card
                item {
                    WhatsAppStatusCardHindi()
                }

                // Categories Section
                item {
                    Text(
                        text = "श्रेणियां",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories) { category ->
                            CategoryCardHindi(category = category)
                        }
                    }
                }

                // Tab Section
                item {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF00B09C)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
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

                // Status Grid - Use the working StatusGrid from components
                item {
                    com.mg.statussaver.presentation.screens.components.StatusGrid(
                        statuses = statusItems.map {
                            // Convert StatusItem to Status_domain for compatibility
                            com.mg.statussaver.domain.model.Status_domain(
                                id = it.path, // or any unique string
                                thumbnailUri = it.path,
                                isVideo = it.type == MediaType.VIDEO,
                                duration = null, // Not available in StatusItem
                                timestamp = it.timestamp,
                                isDownloaded = false, // Not available in StatusItem
                                filePath = it.path
                            )
                        },
                        onDownloadClick = { /* Handle download */ },
                        isSaved = selectedTabIndex == 3
                    )
                }
            }
        }
    }
}

@Composable
fun WhatsAppStatusCardHindi() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // TODO: Navigate to Chat Screen
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF00B09C)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = "चैट",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "डायरेक्ट चैट",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "बिना कॉन्टैक्ट सेव किए मैसेज भेजें",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "खोलें",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CategoryCardHindi(category: CategoryItem) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { /* Navigate to category */ },
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
                text = category.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = "${category.count} आइटम",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenHindiPreview() {
    MaterialTheme {
        HomeScreenHindi()
    }
}
