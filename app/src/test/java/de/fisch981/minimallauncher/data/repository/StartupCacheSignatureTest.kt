package de.fisch981.minimallauncher.data.repository

import de.fisch981.minimallauncher.data.repository.StartupCache.Companion.SnapshotSignatureEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StartupCacheSignatureTest {

    private fun sampleEntry(
        pkg: String = "com.example.app",
        act: String = "MainActivity",
        label: String = "App",
        originalLabel: String = "App",
        userHash: Int = 0,
        iconHash: Int = 123,
        subApps: List<SnapshotSignatureEntry> = emptyList()
    ) = SnapshotSignatureEntry(
        packageName = pkg,
        activityName = act,
        label = label,
        originalLabel = originalLabel,
        userHash = userHash,
        iconIdentityHash = iconHash,
        subApps = subApps
    )

    @Test
    fun identicalFavoritesAndLettersProduceIdenticalSignatureRegardlessOfLetterOrder() {
        val sigA = StartupCache.buildSnapshotSignatureFromEntries(
            availableLetters = setOf('B', 'A', 'C'),
            favorites = listOf(sampleEntry(pkg = "a"), sampleEntry(pkg = "b"))
        )
        val sigB = StartupCache.buildSnapshotSignatureFromEntries(
            availableLetters = setOf('C', 'A', 'B'),
            favorites = listOf(sampleEntry(pkg = "a"), sampleEntry(pkg = "b"))
        )
        assertEquals(sigA, sigB)
    }

    @Test
    fun changingFavoriteLabelOrIconIdentityChangesSignature() {
        val base = StartupCache.buildSnapshotSignatureFromEntries(
            availableLetters = setOf('A'),
            favorites = listOf(sampleEntry(label = "Mail", iconHash = 10))
        )
        val renamed = StartupCache.buildSnapshotSignatureFromEntries(
            availableLetters = setOf('A'),
            favorites = listOf(sampleEntry(label = "Inbox", iconHash = 10))
        )
        val newIcon = StartupCache.buildSnapshotSignatureFromEntries(
            availableLetters = setOf('A'),
            favorites = listOf(sampleEntry(label = "Mail", iconHash = 11))
        )
        assertNotEquals(base, renamed)
        assertNotEquals(base, newIcon)
    }

    @Test
    fun changingSubAppsOrAvailableLettersChangesSignature() {
        val base = StartupCache.buildSnapshotSignatureFromEntries(
            availableLetters = setOf('A', 'B'),
            favorites = listOf(
                sampleEntry(
                    pkg = "folder.host",
                    subApps = listOf(sampleEntry(pkg = "sub.one"))
                )
            )
        )
        val subChanged = StartupCache.buildSnapshotSignatureFromEntries(
            availableLetters = setOf('A', 'B'),
            favorites = listOf(
                sampleEntry(
                    pkg = "folder.host",
                    subApps = listOf(sampleEntry(pkg = "sub.one"), sampleEntry(pkg = "sub.two"))
                )
            )
        )
        val lettersChanged = StartupCache.buildSnapshotSignatureFromEntries(
            availableLetters = setOf('A', 'B', 'C'),
            favorites = listOf(
                sampleEntry(
                    pkg = "folder.host",
                    subApps = listOf(sampleEntry(pkg = "sub.one"))
                )
            )
        )
        assertNotEquals(base, subChanged)
        assertNotEquals(base, lettersChanged)
    }
}
