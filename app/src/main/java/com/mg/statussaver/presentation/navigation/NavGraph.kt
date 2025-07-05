package com.mg.statussaver.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mg.statussaver.presentation.screens.splash.SplashScreen
import com.mg.statussaver.presentation.screens.language.LanguageSelectionScreen
import com.mg.statussaver.presentation.screens.home.HomeScreen
import com.mg.statussaver.presentation.screens.home.HomeScreenHindi
import com.mg.statussaver.presentation.screens.saved.SavedStatusScreen
import com.mg.statussaver.presentation.screens.settings.SettingsScreen
import com.mg.statussaver.presentation.screens.directchat.DirectChatScreen

object Routes {
    const val SPLASH = "splash"
    const val LANGUAGE = "language"
    const val HOME = "home"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val HOME_HINDI = "home_hindi"
    const val DIRECT_CHAT = "direct_chat"
}

@Composable
fun StatusSaverNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }
        composable(Routes.LANGUAGE) {
            LanguageSelectionScreen(navController)
        }
        composable(Routes.HOME) {
            HomeScreen(
                onStatusClick = { statusItem ->
                    // Handle status click - could navigate to full screen view
                },
                onDirectChatClick = {
                    navController.navigate(Routes.DIRECT_CHAT)
                },
                onNavigateToSaved = {
                    navController.navigate(Routes.SAVED)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToLanguage = {
                    navController.navigate(Routes.LANGUAGE)
                }
            )
        }
        composable(Routes.SAVED) {
            SavedStatusScreen(navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(navController)
        }
        composable(Routes.HOME_HINDI) {
            HomeScreenHindi(navController)
        }
        composable(Routes.DIRECT_CHAT) {
            DirectChatScreen(navController)
        }
    }
}