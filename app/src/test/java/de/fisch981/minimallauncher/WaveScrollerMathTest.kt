package de.fisch981.minimallauncher

import org.junit.Assert.assertEquals
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
}
