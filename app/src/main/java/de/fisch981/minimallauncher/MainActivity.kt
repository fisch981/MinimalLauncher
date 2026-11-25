package de.fisch981.minimallauncher

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import de.fisch981.minimallauncher.ui.screens.HomeScreen
import de.fisch981.minimallauncher.ui.theme.MinimalLauncherTheme
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    private val _wakeFadeAlphaHidden = MutableStateFlow(false)
    private val _wakeFadeInTrigger = MutableStateFlow(0)
    private var isActivityResumed = false
    private var pendingWakeFadeIn = false
    private var lastUnlockElapsedMs = 0L
    private var isScreenReceiverRegistered = false

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> prepareForWakeFadeIn()
                Intent.ACTION_USER_PRESENT -> {
                    lastUnlockElapsedMs = SystemClock.elapsedRealtime()
                    if (isActivityResumed) {
                        tryTriggerWakeFadeIn()
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    if (!isDeviceLocked()) {
                        lastUnlockElapsedMs = SystemClock.elapsedRealtime()
                        if (isActivityResumed) {
                            tryTriggerWakeFadeIn()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        runCatching {
            ContextCompat.registerReceiver(this, screenStateReceiver, screenFilter, ContextCompat.RECEIVER_EXPORTED)
            isScreenReceiverRegistered = true
        }

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
                val wakeFadeInEnabled by viewModel.wakeFadeInEnabled.collectAsState()
                val wakeFadeInDurationMs by viewModel.wakeFadeInDurationMs.collectAsState()
                val wakeFadeAlphaHidden by _wakeFadeAlphaHidden.collectAsState()
                val wakeFadeInTrigger by _wakeFadeInTrigger.collectAsState()
                val wakeAlpha = remember { Animatable(1f) }

                LaunchedEffect(wakeFadeInEnabled, wakeFadeAlphaHidden) {
                    if (!wakeFadeInEnabled) {
                        wakeAlpha.snapTo(1f)
                    } else if (wakeFadeAlphaHidden) {
                        wakeAlpha.snapTo(0f)
                    }
                }

                LaunchedEffect(wakeFadeInTrigger) {
                    if (wakeFadeInTrigger > 0 && wakeFadeInEnabled) {
                        wakeAlpha.snapTo(0f)
                        wakeAlpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = wakeFadeInDurationMs.coerceIn(200, 2000),
                                easing = FastOutSlowInEasing
                            )
                        )
                    } else if (!wakeFadeAlphaHidden) {
                        wakeAlpha.snapTo(1f)
                    }
                }

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
                    HomeScreen(
                        viewModel = viewModel,
                        modifier = Modifier.graphicsLayer { alpha = wakeAlpha.value }
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyStatusBarVisibility(viewModel.hideStatusBar.value)
            tryTriggerWakeFadeIn()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onReturnToLauncher(closeSettings = true)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onLauncherStart()
    }

    override fun onResume() {
        super.onResume()
        isActivityResumed = true
        viewModel.checkDefaultLauncher()
        viewModel.refreshMediaListener()
        viewModel.refreshNotifications()
        viewModel.onReturnToLauncher(closeSettings = false)
        tryTriggerWakeFadeIn()
    }

    override fun onPause() {
        isActivityResumed = false
        super.onPause()
    }

    override fun onStop() {
        viewModel.onLauncherStop()
        super.onStop()
    }

    override fun onDestroy() {
        if (isScreenReceiverRegistered) {
            runCatching { unregisterReceiver(screenStateReceiver) }
            isScreenReceiverRegistered = false
        }
        super.onDestroy()
    }

    private fun isDeviceLocked(): Boolean {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.isKeyguardLocked == true
    }

    private fun prepareForWakeFadeIn() {
        if (viewModel.wakeFadeInEnabled.value) {
            pendingWakeFadeIn = true
            lastUnlockElapsedMs = 0L
            _wakeFadeAlphaHidden.value = true
        }
    }

    private fun tryTriggerWakeFadeIn() {
        if (!pendingWakeFadeIn) {
            _wakeFadeAlphaHidden.value = false
            return
        }
        if (!viewModel.wakeFadeInEnabled.value) {
            pendingWakeFadeIn = false
            _wakeFadeAlphaHidden.value = false
            return
        }
        if (isDeviceLocked()) {
            return
        }
        if (lastUnlockElapsedMs > 0L && SystemClock.elapsedRealtime() - lastUnlockElapsedMs > 1500L) {
            pendingWakeFadeIn = false
            _wakeFadeAlphaHidden.value = false
            return
        }
        pendingWakeFadeIn = false
        _wakeFadeAlphaHidden.value = false
        _wakeFadeInTrigger.value = _wakeFadeInTrigger.value + 1
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
