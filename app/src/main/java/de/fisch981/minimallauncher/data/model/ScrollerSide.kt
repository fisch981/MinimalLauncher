package de.fisch981.minimallauncher.data.model

enum class ScrollerSide {
    LEFT,
    RIGHT,
    BOTH;

    val hasLeft: Boolean get() = this == LEFT || this == BOTH
    val hasRight: Boolean get() = this == RIGHT || this == BOTH

    companion object {
        fun fromStored(value: String?, fallbackRightHanded: Boolean): ScrollerSide {
            return entries.firstOrNull { it.name == value }
                ?: if (fallbackRightHanded) RIGHT else LEFT
        }
    }
}
