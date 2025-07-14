package com.mg.statussaver.presentation.screens.settings

import android.Manifest.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.mg.statussaver.ui.theme.TealPrimary

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var notificationEnabled by remember { mutableStateOf(false) }
    var autoSaveEnabled by remember { mutableStateOf(false) }

    val requiredPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            notificationEnabled = false // Disable toggle if permission is denied
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        item {
            // Top Bar Title
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Language Section
        item {
            SettingsCard(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = "Change your language",
                trailingText = "English",
                onClick = { navController.navigate("language") }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Notification Section
        item {
            SettingsCard(
                icon = Icons.Default.Notifications,
                title = "Notification",
                subtitle = "Receive new status alerts",
                trailingContent = {
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { enabled ->
                            notificationEnabled = enabled
                            if (enabled) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    requiredPermissionLauncher.launch(permission.POST_NOTIFICATIONS)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TealPrimary,
                            checkedTrackColor = TealPrimary.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Auto Save Section
        item {
            SettingsCard(
                icon = Icons.Default.Download,
                title = "Auto Save",
                subtitle = "Automatically save all new statuses",
                trailingContent = {
                    Switch(
                        checked = autoSaveEnabled,
                        onCheckedChange = { autoSaveEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TealPrimary,
                            checkedTrackColor = TealPrimary.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Save Folder Section
        item {
            SettingsCard(
                icon = Icons.Default.Folder,
                title = "Save Statuses in Folder",
                subtitle = "/storage/emulated/0/Download/StatusSaver",
                onClick = { /* Folder selector */ }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Privacy Policy Section
        item {
            SettingsCard(
                icon = Icons.Default.Description,
                title = "Privacy Policy",
                subtitle = "Terms and conditions",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://mobiforgetech.blogspot.com/p/privacy-policy.html".toUri()
                    )
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Share App Section
        item {
            SettingsCard(
                icon = Icons.Default.Share,
                title = "Share App",
                subtitle = "Spread the joy: Share with friends.",
                onClick = { /* Share app */ }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Rate App Section
        item {
            SettingsCard(
                title = "Rate App",
                icon = Icons.Default.Star,
                subtitle = "Rate us positively",
                onClick = { /* Rate app */ }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Feedback Section
        item {
            SettingsCard(
                icon = Icons.Default.ChatBubble,
                title = "Feedback",
                subtitle = "Share your experience",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://mobiforgetech.blogspot.com/p/privacy-policy.html#contact".toUri()
                    )
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Follow Us Section
        item {
            SettingsCard(
                icon = Icons.Default.Facebook,
                title = "Follow Us",
                subtitle = "Get connect with us",
                onClick = { /* Follow us */ }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // About Section
        item {
            SettingsCard(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "Version : 1.0",
                onClick = { /* About */ }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Back Button
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary
                )
            ) {
                Text(
                    text = "Back",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Title and Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Trailing content
            if (trailingContent != null) {
                trailingContent()
            } else if (trailingText != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trailingText,
                        color = TealPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}