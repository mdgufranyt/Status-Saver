package com.mg.statussaver.presentation.screens.language

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mg.statussaver.data.preferences.LanguagePreferences

/**
 * Data class representing a language option
 */
data class Language(
    val code: String,
    val displayName: String
)


/**

 * Language Selection Screen for WhatsApp Status Saver App
 * Features modern UI with top bar and comprehensive Indian language support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(navController: NavController? = null) {
    // List of Indian languages with their display names
    val languages = listOf(
        Language("en", "English"),
        Language("hi", "हिन्दी (Hindi) - Coming Soon"),
        Language("ur", "اُردُو‎ (Urdu) - Coming Soon"),
        Language("bn", "বাংলা (Bengali) - Coming Soon"),
        Language("ta", "தமிழ் (Tamil) - Coming Soon"),
        Language("te", "తెలుగు (Telugu) - Coming Soon"),
        Language("mr", "मराठी (Marathi) - Coming Soon"),
        Language("gu", "ગુજરાતી (Gujarati) - Coming Soon"),
        Language("kn", "ಕನ್ನಡ (Kannada) - Coming Soon"),
        Language("ml", "മലയാളം (Malayalam) - Coming Soon"),
    )

    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize().background(Color.Black)
//            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar with Title and Continue Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Title and Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                // Subtitle
                Text(
                    text = "Choose your preferred language",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Right side - Continue Icon Button
            IconButton(
                onClick = {
                    if (selectedLanguage != null) {
                        // Save the selected language
                        val languagePreferences = LanguagePreferences(context)
                        languagePreferences.saveSelectedLanguage(selectedLanguage!!.code)

                        // Navigate to home screen
                        navController?.navigate("home") {
                            popUpTo("language") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Please select a language", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = selectedLanguage != null,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (selectedLanguage != null)
                            Color(0xFF00B09C)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Continue",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Divider line
//        HorizontalDivider(
//            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
//            thickness = 1.dp
//        )

        // Language List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Language items
            items(languages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onLanguageSelected = { selectedLanguage = language }
                )
            }

            // Bottom spacing
            item {

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Individual language item component
 */
@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onLanguageSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLanguageSelected() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onLanguageSelected,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = language.displayName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Preview function for the Language Selection Screen
 */
@Preview(showBackground = true)
@Composable
fun LanguageSelectionScreenPreview() {
    MaterialTheme {
        LanguageSelectionScreen()
    }
}