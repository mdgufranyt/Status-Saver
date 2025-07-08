package com.mg.statussaver.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import com.mg.statussaver.presentation.screens.status.StatusViewerScreen
import com.mg.statussaver.presentation.screens.home.HomeViewModel
import com.mg.statussaver.presentation.screens.home.shareStatusFile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import com.mg.statussaver.presentation.screens.home.HomeUiState

object Routes {
    const val SPLASH = "splash"
    const val LANGUAGE = "language"
    const val HOME = "home"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val HOME_HINDI = "home_hindi"
    const val DIRECT_CHAT = "direct_chat"
    const val VIEWER = "viewer"
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
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                onStatusClick = { item ->
                    val list = (viewModel.uiState.value as? HomeUiState.Success)?.statusItems ?: return@HomeScreen
                    viewModel.openViewer(viewModel.selectedTabIndex.value, item, list)
                    navController.navigate(Routes.VIEWER)
                },
                onDirectChatClick = {
                    navController.navigate(Routes.DIRECT_CHAT)
                },
                onNavigateToDownloads = {
                    navController.navigate(Routes.SAVED)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToLanguage = {
                    navController.navigate(Routes.LANGUAGE)
                },
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

        composable(Routes.VIEWER) { navBackStackEntry ->

            val parentEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }
            val context = LocalContext.current
            val viewModel: HomeViewModel = hiltViewModel(parentEntry)

            val statusList by viewModel.viewerItems.collectAsState()
            val initialIndex by viewModel.initialViewerIndex.collectAsState()

            StatusViewerScreen(
                statusList = statusList,
                initialIndex = initialIndex,
                onBack = { navController.popBackStack() },
                onShare = { item -> shareStatusFile(context, item.path, item.type) },
                onDownload = { item ->
                    viewModel.downloadStatus(item) { success, errorMsg ->
                        val msg = if (success) "Status downloaded" else errorMsg ?: "Download failed"
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }



}