package de.fisch981.minimallauncher.data.nova

/**
 * Minimale, von Android unabhängige Sicht auf die SQLite-Datenbank (`nova.db`) eines Nova-Backups.
 * Dadurch lässt sich der Parser auf der JVM gegen eine echte Backup-Datei testen.
 */
interface NovaDb {
    /** Führt ein SELECT aus. Werte: null, Long, Double, String oder ByteArray. */
    fun query(sql: String): List<List<Any?>>
}

/** Ein von Nova gespeichertes App-Icon (fertig gerendertes PNG). */
class NovaIcon(
    /** Normalisiert: `paket/vollqualifizierte.Activity` */
    val component: String,
    val png: ByteArray,
    /** Name des Drawables im Icon-Pack (z. B. `calculator`), falls aus der Quelle ablesbar. */
    val packDrawable: String?
)

class NovaParseResult(
    val icons: List<NovaIcon>,
    /** Paketname des in Nova aktiven Icon-Packs, z. B. `com.natewren.lines`. */
    val iconPackPackage: String?,
    /** Dock- und primäre Homescreen-App-Komponenten aus Nova (in Anzeigereihenfolge). */
    val favoriteComponents: List<String> = emptyList(),
    /** Benutzerdefinierte App-Namen (`component` -> `title`) aus Nova. */
    val customLabels: Map<String, String> = emptyMap()
)

/**
 * Liest die Icon-Zuordnung aus einem Nova-Backup. Format (aus echtem Backup abgeleitet):
 *
 * - `allapps.componentName` = `paket/activity` (Activity kann mit `.` abgekürzt sein),
 *   `allapps.customIconSource` = `icontheme_content_table://com.teslacoilsw.launcher.settings/customIcons/<id>`
 * - `favorites.intent` enthält `component=paket/activity`, `favorites.customIconSource` wie oben
 * - `customIcons._id` / `customIcons.icon` (PNG-BLOB) / `customIcons.source`
 *   (`content://com.natewren.lines.fileProvider/cache/<name>.png` oder
 *   `android.resource://<paket>/drawable/<name>` oder NULL)
 * - `nova.xml`: `<string name="theme_icon_pack">Lines:GO:com.natewren.lines</string>`
 */
object NovaBackupParser {

    const val ICON_URI_PREFIX = "icontheme_content_table://com.teslacoilsw.launcher.settings/customIcons/"

    private val PNG_MAGIC = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)

    private val SQL_APPS = """
        SELECT a.componentName, ci.source, ci.icon
        FROM allapps a
        JOIN customIcons ci
          ON ci._id = CAST(SUBSTR(a.customIconSource, ${ICON_URI_PREFIX.length + 1}) AS INTEGER)
        WHERE a.customIconSource LIKE '$ICON_URI_PREFIX%'
    """.trimIndent()

    private val SQL_FAVORITES = """
        SELECT f.intent, ci.source, ci.icon
        FROM favorites f
        JOIN customIcons ci
          ON ci._id = CAST(SUBSTR(f.customIconSource, ${ICON_URI_PREFIX.length + 1}) AS INTEGER)
        WHERE f.customIconSource LIKE '$ICON_URI_PREFIX%'
    """.trimIndent()

    private val SQL_HOME_FAVORITES = """
        SELECT intent FROM favorites
        WHERE container IN (-101, -100) AND itemType = 0 AND intent IS NOT NULL
        ORDER BY container ASC, screen ASC, cellY ASC, cellX ASC
    """.trimIndent()

    private val SQL_CUSTOM_LABELS = """
        SELECT componentName, title FROM allapps
        WHERE title IS NOT NULL AND title != ''
    """.trimIndent()

    fun parse(db: NovaDb, novaXml: String?): NovaParseResult {
        val byComponent = LinkedHashMap<String, NovaIcon>()

        // App-Drawer-Icons haben Vorrang, Favoriten (Homescreen) füllen Lücken.
        for (row in db.query(SQL_APPS)) {
            addRow(byComponent, normalizeComponent(row[0] as? String), row)
        }
        for (row in db.query(SQL_FAVORITES)) {
            addRow(byComponent, componentFromIntent(row[0] as? String), row)
        }

        val favorites = LinkedHashSet<String>()
        for (row in db.query(SQL_HOME_FAVORITES)) {
            val comp = componentFromIntent(row[0] as? String) ?: continue
            if (!comp.startsWith("com.teslacoilsw.launcher/")) {
                favorites.add(comp)
            }
        }

        val customLabels = LinkedHashMap<String, String>()
        for (row in db.query(SQL_CUSTOM_LABELS)) {
            val comp = normalizeComponent(row[0] as? String) ?: continue
            val title = (row[1] as? String)?.trim()
            if (!title.isNullOrBlank()) {
                customLabels[comp] = title
            }
        }

        return NovaParseResult(
            icons = byComponent.values.toList(),
            iconPackPackage = iconPackFromXml(novaXml),
            favoriteComponents = favorites.toList(),
            customLabels = customLabels
        )
    }

    private fun addRow(target: MutableMap<String, NovaIcon>, component: String?, row: List<Any?>) {
        if (component == null || target.containsKey(component)) return
        val png = row[2] as? ByteArray ?: return
        if (!isPng(png)) return
        target[component] = NovaIcon(component, png, packDrawableFromSource(row[1] as? String))
    }

    fun isPng(bytes: ByteArray): Boolean =
        bytes.size > 8 && PNG_MAGIC.indices.all { bytes[it] == PNG_MAGIC[it] }

    /** `paket/.Klasse` → `paket/paket.Klasse`; liefert null bei ungültiger Eingabe. */
    fun normalizeComponent(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val value = raw.trim().removePrefix("ComponentInfo{").removeSuffix("}")
        val slash = value.indexOf('/')
        if (slash <= 0 || slash == value.length - 1) return null
        val pkg = value.substring(0, slash)
        val cls = value.substring(slash + 1)
        return if (cls.startsWith(".")) "$pkg/$pkg$cls" else "$pkg/$cls"
    }

    /** Liest `component=paket/activity` aus einer Intent-URI (`#Intent;...;component=...;end`). */
    fun componentFromIntent(intent: String?): String? {
        if (intent.isNullOrBlank()) return null
        val match = Regex("component=([^;]+)").find(intent) ?: return null
        return normalizeComponent(match.groupValues[1])
    }

    fun packDrawableFromSource(source: String?): String? {
        if (source.isNullOrBlank()) return null
        Regex("^content://[^/]+/cache/(.+)\\.png$").find(source)?.let { return it.groupValues[1] }
        Regex("^android\\.resource://[^/]+/drawable/(.+)$").find(source)?.let { return it.groupValues[1] }
        return null
    }

    /** `Lines:GO:com.natewren.lines` → `com.natewren.lines` */
    fun iconPackFromXml(xml: String?): String? {
        if (xml.isNullOrBlank()) return null
        val value = Regex("<string name=\"theme_icon_pack\">([^<]*)</string>")
            .find(xml)?.groupValues?.get(1)?.trim() ?: return null
        val pkg = value.substringAfterLast(':')
        return pkg.takeIf { it.contains('.') }
    }
}
