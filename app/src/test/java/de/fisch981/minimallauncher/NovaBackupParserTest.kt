package de.fisch981.minimallauncher

import de.fisch981.minimallauncher.data.nova.NovaBackupParser
import de.fisch981.minimallauncher.data.nova.NovaDb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.sql.DriverManager

class NovaBackupParserTest {

    // ---------- reine Logik ----------

    @Test
    fun normalizeComponent_expandsRelativeActivity() {
        assertEquals(
            "com.android.calculator2/com.android.calculator2.Calculator",
            NovaBackupParser.normalizeComponent("com.android.calculator2/.Calculator")
        )
        assertEquals(
            "com.android.calendar/com.android.calendar.AllInOneActivity",
            NovaBackupParser.normalizeComponent("com.android.calendar/com.android.calendar.AllInOneActivity")
        )
        assertNull(NovaBackupParser.normalizeComponent("kein-slash"))
        assertNull(NovaBackupParser.normalizeComponent(null))
    }

    @Test
    fun componentFromIntent_readsComponent() {
        val intent = "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "launchFlags=0x10200000;component=com.android.deskclock/.DeskClock;end"
        assertEquals("com.android.deskclock/com.android.deskclock.DeskClock", NovaBackupParser.componentFromIntent(intent))
    }

    @Test
    fun packDrawable_fromSources() {
        assertEquals("calculator", NovaBackupParser.packDrawableFromSource("content://com.natewren.lines.fileProvider/cache/calculator.png"))
        assertEquals("calendar", NovaBackupParser.packDrawableFromSource("android.resource://com.natewren.lines/drawable/calendar"))
        assertNull(NovaBackupParser.packDrawableFromSource(null))
    }

    @Test
    fun iconPack_fromNovaXml() {
        val xml = """<map><string name="theme_icon_pack">Lines:GO:com.natewren.lines</string></map>"""
        assertEquals("com.natewren.lines", NovaBackupParser.iconPackFromXml(xml))
    }

    // ---------- gegen das echte Backup (samples/ ist gitignored, Test wird sonst übersprungen) ----------

    private fun realFile(vararg path: String): File? =
        File("../samples/nova", path.joinToString("/")).takeIf { it.isFile }

    @Test
    fun realBackup_importsAllCustomIcons() {
        val dbFile = realFile("nova.db")
        assumeTrue("Kein Beispiel-Backup unter samples/nova", dbFile != null)

        DriverManager.getConnection("jdbc:sqlite:${dbFile!!.absolutePath}").use { conn ->
            val db = object : NovaDb {
                override fun query(sql: String): List<List<Any?>> {
                    val rs = conn.createStatement().executeQuery(sql)
                    val n = rs.metaData.columnCount
                    val rows = ArrayList<List<Any?>>()
                    while (rs.next()) rows.add(List(n) { rs.getObject(it + 1) })
                    return rows
                }
            }
            val xml = realFile("nova.xml")?.readText()
            val result = NovaBackupParser.parse(db, xml)

            println("Importierte Icons: ${result.icons.size}, Icon-Pack: ${result.iconPackPackage}")
            println("davon mit Lines-Drawable-Namen: ${result.icons.count { it.packDrawable != null }}")

            assertEquals("com.natewren.lines", result.iconPackPackage)
            // Direkt aus der DB gezählt: 343 Drawer-Apps mit eigenem Icon (+ Favoriten, die nicht im Drawer-Set sind)
            assertTrue("zu wenige Icons: ${result.icons.size}", result.icons.size >= 330)
            assertTrue(result.icons.all { NovaBackupParser.isPng(it.png) })
            assertTrue(result.icons.all { it.component.contains('/') && !it.component.contains("/.") })

            assertTrue(result.icons.any { it.packDrawable == null && NovaBackupParser.isPng(it.png) })

            assertTrue(result.icons.count { it.packDrawable != null } > 200)
            assertTrue("Dock/Desktop Favoriten fehlen", result.favoriteComponents.isNotEmpty())
            assertTrue(result.favoriteComponents.all { it.contains('/') && !it.contains("/.") })
        }
    }
}
