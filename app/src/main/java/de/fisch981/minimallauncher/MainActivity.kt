package de.fisch981.minimallauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import de.fisch981.minimallauncher.ui.screens.HomeScreen
import de.fisch981.minimallauncher.ui.theme.MinimalLauncherTheme
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MinimalLauncherTheme {
                val isSearchOpen by viewModel.isSearchOpen.collectAsState()
                val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
                val isHiddenAppsOpen by viewModel.isHiddenAppsOpen.collectAsState()
                val activeSettingsCategory by viewModel.activeSettingsCategory.collectAsState()
                val isManageFavoritesOpen by viewModel.isManageFavoritesOpen.collectAsState()
                val appForIconEditing by viewModel.appForIconEditing.collectAsState()
                val appForFolderEditing by viewModel.appForFolderEditing.collectAsState()
                val appForRenaming by viewModel.appForRenaming.collectAsState()
                val selectedPopup by viewModel.selectedAppForPopup.collectAsState()
                val hideStatusBar by viewModel.hideStatusBar.collectAsState()

                LaunchedEffect(hideStatusBar) {
                    applyStatusBarVisibility(hideStatusBar)
                }

                // Crucial Launcher Rule: The Back button must NEVER exit the Launcher!
                BackHandler(enabled = true) {
                    when {
                        isSearchOpen -> viewModel.setSearchOpen(false)
                        isManageFavoritesOpen -> viewModel.setManageFavoritesOpen(false)
                        isHiddenAppsOpen -> viewModel.setHiddenAppsOpen(false)
                        activeSettingsCategory != null -> viewModel.openSettingsCategory(null)
                        isSettingsOpen -> viewModel.setSettingsOpen(false)
                        appForIconEditing != null -> viewModel.closeIconEditor()
                        appForFolderEditing != null -> viewModel.closeFolderEditor()
                        appForRenaming != null -> viewModel.closeRenameDialog()
                        selectedPopup != null -> viewModel.closePopup()
                        else -> {
                            // If in App Drawer, return to Home (Favorites only)
                            viewModel.requestScrollToTop()
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = PureBlack
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyStatusBarVisibility(viewModel.hideStatusBar.value)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onReturnToLauncher(closeSettings = true)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDefaultLauncher()
        viewModel.refreshMediaListener()
        viewModel.refreshNotifications()
        viewModel.onReturnToLauncher(closeSettings = false)
    }

    private fun applyStatusBarVisibility(hide: Boolean) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (hide) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}
