package de.fisch981.minimallauncher.data.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import de.fisch981.minimallauncher.data.model.MediaState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MediaSessionHelper(private val context: Context) {

    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    private val notificationListenerComponent = ComponentName(context, LauncherNotificationService::class.java)

    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    private var activeController: MediaController? = null
    @Volatile
    private var isListening = false

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateMediaState()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateMediaState()
        }

        override fun onSessionDestroyed() {
            activeController = null
            _mediaState.value = MediaState()
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveController(controllers)
    }

    fun startListening() {
        if (!LauncherNotificationService.isPermissionGranted(context) || mediaSessionManager == null) {
            if (isListening) {
                stopListening()
            }
            return
        }
        try {
            if (isListening) {
                val controllers = mediaSessionManager.getActiveSessions(notificationListenerComponent)
                updateActiveController(controllers)
                return
            }
            mediaSessionManager.addOnActiveSessionsChangedListener(
                sessionsChangedListener,
                notificationListenerComponent
            )
            isListening = true
            val controllers = mediaSessionManager.getActiveSessions(notificationListenerComponent)
            updateActiveController(controllers)
        } catch (_: Exception) {
            isListening = false
        }
    }

    fun stopListening(clearState: Boolean = true) {
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
            activeController?.unregisterCallback(controllerCallback)
            activeController = null
            if (clearState) {
                _mediaState.value = MediaState()
            }
        } catch (_: Exception) {
        } finally {
            isListening = false
        }
    }

    private fun updateActiveController(controllers: List<MediaController>?) {
        activeController?.unregisterCallback(controllerCallback)
        activeController = controllers?.firstOrNull()
        activeController?.registerCallback(controllerCallback)
        updateMediaState()
    }

    private fun updateMediaState() {
        val controller = activeController
        if (controller == null) {
            _mediaState.value = MediaState()
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val pkg = controller.packageName

        _mediaState.value = MediaState(
            isPlaying = isPlaying,
            title = title,
            artist = artist,
            packageName = pkg
        )
    }

    fun togglePlayPause() {
        val controller = activeController ?: return
        val state = controller.playbackState?.state
        if (state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    fun skipToNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipToPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }
}
