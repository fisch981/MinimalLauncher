package de.fisch981.minimallauncher.data.model

data class MediaState(
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val packageName: String? = null
) {
    val hasActiveMedia: Boolean get() = isPlaying || (!title.isNullOrBlank())
}
