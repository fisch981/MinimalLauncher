package de.fisch981.minimallauncher.data.model

enum class HomeGestureAction(val code: String) {
    NONE("none"),
    EXPAND_NOTIFICATIONS("notifications"),
    SEARCH("search"),
    ALL_APPS("all_apps");

    companion object {
        fun fromCode(code: String?, default: HomeGestureAction = NONE): HomeGestureAction =
            entries.firstOrNull {
                it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true)
            } ?: default
    }
}
