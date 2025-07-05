package com.mg.statussaver.presentation.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.mg.statussaver.R
import com.mg.statussaver.data.preferences.LanguagePreferences

/**
 * Splash Screen Component for WhatsApp Status Saver App
 * Features full-screen background image with centered logo and app branding
 * Automatically navigates based on language selection status
 */
@Composable
fun SplashScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val languagePreferences = remember { LanguagePreferences(context) }

    // Auto-navigation effect - triggers after 1 second
    LaunchedEffect(Unit) {
        delay(1000) // 1 seconds delay

        // Always navigate to home screen (no language selection for first time users)
        navController?.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // Main splash screen container
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background Image - fills entire screen
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = "Splash background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Ensures image covers entire screen
        )

        // Content overlay with logo and text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Main Logo Image
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = "WhatsApp Status Saver Logo",
                modifier = Modifier
                    .size(120.dp) // Adjust size as needed
                    .padding(bottom = 24.dp)
            )

            // App Name
            Text(
                text = "WhatsApp Status Saver",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // App Tagline/Subtitle
            Text(
                text = "Save & Share Status Easily",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Preview function for the Splash Screen
 * Shows how the screen will look in design mode
 */
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen()
    }
}
