package de.fisch981.minimallauncher.ui.components

import kotlin.math.roundToInt

internal data class DrawerLetterSection(
    val headerIndex: Int,
    val appCount: Int
)

internal object DrawerScrollMath {
    const val MIN_HEADER_TOP_FRACTION = 0.28f
    const val MAX_HEADER_TOP_FRACTION = 0.48f

    /**
     * Resolves the drawer section for [letter], falling forward to the next populated letter
     * or backward to the nearest preceding populated letter if needed.
     */
    fun resolveLetterSection(
        letter: Char,
        letterSections: Map<Char, DrawerLetterSection>,
        alphabetOrder: List<Char>
    ): DrawerLetterSection? {
        letterSections[letter]?.let { return it }
        if (letterSections.isEmpty() || alphabetOrder.isEmpty()) return null

        val startPos = alphabetOrder.indexOf(letter).coerceAtLeast(0)
        for (i in startPos until alphabetOrder.size) {
            letterSections[alphabetOrder[i]]?.let { return it }
        }
        for (i in (startPos - 1) downTo 0) {
            letterSections[alphabetOrder[i]]?.let { return it }
        }
        return null
    }

    /**
     * Computes the desired viewport-top Y coordinate (in pixels) for a section's letter header
     * so that its app entries appear vertically centered in the screen, clamped between 28% and 48%
     * of the viewport height so large sections keep their header visible above center.
     */
    fun calculateHeaderTopOffsetPx(
        viewportHeightPx: Int,
        headerHeightPx: Float,
        estimatedRowHeightPx: Float,
        appCount: Int
    ): Int {
        if (viewportHeightPx <= 0 || appCount <= 0) return 0

        val estimatedSectionHeightPx = headerHeightPx + appCount * estimatedRowHeightPx
        val idealTopPx = (viewportHeightPx - estimatedSectionHeightPx) / 2f
        val minTopPx = viewportHeightPx * MIN_HEADER_TOP_FRACTION
        val maxTopPx = viewportHeightPx * MAX_HEADER_TOP_FRACTION

        return idealTopPx.coerceIn(minTopPx, maxTopPx).roundToInt()
    }
}
