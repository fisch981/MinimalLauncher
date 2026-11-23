package de.fisch981.minimallauncher

import de.fisch981.minimallauncher.ui.components.DrawerLetterSection
import de.fisch981.minimallauncher.ui.components.DrawerScrollMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.exp

class WaveScrollerMathTest {

    @Test
    fun testGaussianWaveDisplacement() {
        val sigma = 60.0
        val maxOffset = 32.0

        // At exact touch point (distance = 0)
        val centerGaussian = exp(-0.0 / (2.0 * sigma * sigma))
        val centerOffset = maxOffset * centerGaussian
        assertEquals(1.0, centerGaussian, 0.0001)
        assertEquals(32.0, centerOffset, 0.0001)

        // At distance = sigma
        val sigmaGaussian = exp(-(sigma * sigma) / (2.0 * sigma * sigma))
        val sigmaOffset = maxOffset * sigmaGaussian
        assertTrue(sigmaOffset < centerOffset)
        assertEquals(32.0 * exp(-0.5), sigmaOffset, 0.001)

        // At far distance (distance = 300)
        val farGaussian = exp(-(300.0 * 300.0) / (2.0 * sigma * sigma))
        val farOffset = maxOffset * farGaussian
        assertTrue(farOffset < 0.01)
    }

    @Test
    fun testInitialCharNormalization() {
        fun getInitialChar(label: String): Char {
            val first = label.trim().firstOrNull()?.uppercaseChar() ?: '#'
            return if (first in 'A'..'Z') first else '#'
        }

        assertEquals('A', getInitialChar("Android Auto"))
        assertEquals('N', getInitialChar("Notizen"))
        assertEquals('W', getInitialChar("WhatsApp"))
        assertEquals('#', getInitialChar("1Password"))
        assertEquals('#', getInitialChar("!Special"))
        assertEquals('#', getInitialChar("   "))
    }

    @Test
    fun testDrawerHeaderCenteringOffset() {
        val viewport = 2000
        val headerHeight = 90f
        val rowHeight = 140f

        // Single app section -> section height = 230, ideal = (2000 - 230) / 2 = 885 (within [28%, 48%])
        val singleOffset = DrawerScrollMath.calculateHeaderTopOffsetPx(
            viewportHeightPx = viewport,
            headerHeightPx = headerHeight,
            estimatedRowHeightPx = rowHeight,
            appCount = 1
        )
        assertEquals(885, singleOffset)

        // Three app section -> section height = 510, ideal = (2000 - 510)/2 = 745
        val threeOffset = DrawerScrollMath.calculateHeaderTopOffsetPx(
            viewportHeightPx = viewport,
            headerHeightPx = headerHeight,
            estimatedRowHeightPx = rowHeight,
            appCount = 3
        )
        assertEquals(745, threeOffset)

        // Twenty app section -> large height clamps header at 28% (560) so header remains visible above center
        val largeOffset = DrawerScrollMath.calculateHeaderTopOffsetPx(
            viewportHeightPx = viewport,
            headerHeightPx = headerHeight,
            estimatedRowHeightPx = rowHeight,
            appCount = 20
        )
        assertEquals((viewport * DrawerScrollMath.MIN_HEADER_TOP_FRACTION).toInt(), largeOffset)

        // Invalid viewport -> 0
        assertEquals(0, DrawerScrollMath.calculateHeaderTopOffsetPx(0, headerHeight, rowHeight, 3))
        assertEquals(0, DrawerScrollMath.calculateHeaderTopOffsetPx(viewport, headerHeight, rowHeight, 0))
    }

    @Test
    fun testResolveLetterSectionFallbacks() {
        val alphabet = ('A'..'Z').toList() + listOf('#')
        val sections = mapOf(
            'A' to DrawerLetterSection(headerIndex = 1, appCount = 4),
            'M' to DrawerLetterSection(headerIndex = 10, appCount = 2),
            'W' to DrawerLetterSection(headerIndex = 20, appCount = 1)
        )

        // Exact hit
        assertEquals(
            DrawerLetterSection(10, 2),
            DrawerScrollMath.resolveLetterSection('M', sections, alphabet)
        )

        // Forward fallback (selecting B jumps to M)
        assertEquals(
            DrawerLetterSection(10, 2),
            DrawerScrollMath.resolveLetterSection('B', sections, alphabet)
        )

        // Backward fallback (selecting Z when nothing exists after W lands on W, not A)
        assertEquals(
            DrawerLetterSection(20, 1),
            DrawerScrollMath.resolveLetterSection('Z', sections, alphabet)
        )

        // Empty sections -> null
        assertNull(DrawerScrollMath.resolveLetterSection('A', emptyMap(), alphabet))
    }
}
