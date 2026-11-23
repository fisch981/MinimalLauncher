package de.fisch981.minimallauncher.data.nova

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import java.io.File
import java.util.zip.ZipFile

/** Importiert die Icon-Zuordnung aus einer `.novabackup`-Datei (ZIP mit `nova.db` und `nova.xml`). */
class NovaBackupImporter(
    private val context: Context,
    private val store: ImportedIconStore
) {

    class ImportResult(
        val importedCount: Int,
        val iconPackPackage: String? = null,
        val errorMessage: String? = null,
        val favoriteComponents: List<String> = emptyList(),
        val customLabels: Map<String, String> = emptyMap()
    )

    fun importFromUri(uri: Uri): ImportResult {
        val workDir = File(context.cacheDir, "nova_import").apply {
            deleteRecursively()
            mkdirs()
        }
        try {
            val zipFile = File(workDir, "backup.zip")
            val input = context.contentResolver.openInputStream(uri)
                ?: return ImportResult(0, errorMessage = "Die Datei konnte nicht geöffnet werden.")
            input.use { src -> zipFile.outputStream().use { dst -> src.copyTo(dst) } }

            var dbFile: File? = null
            var xml: String? = null
            try {
                ZipFile(zipFile).use { zip ->
                    val entries = zip.entries().toList().filter { !it.isDirectory }
                    val dbEntry = entries.firstOrNull { it.name.substringAfterLast('/') == "nova.db" }
                        ?: entries.firstOrNull { it.name.endsWith(".db", ignoreCase = true) }
                    if (dbEntry != null) {
                        dbFile = File(workDir, "nova.db").also { out ->
                            zip.getInputStream(dbEntry).use { s -> out.outputStream().use { s.copyTo(it) } }
                        }
                    }
                    entries.firstOrNull { it.name.substringAfterLast('/') == "nova.xml" }?.let { e ->
                        xml = zip.getInputStream(e).bufferedReader().readText()
                    }
                }
            } catch (e: java.util.zip.ZipException) {
                return ImportResult(0, errorMessage = "Das ist keine gültige Nova-Backup-Datei (kein ZIP-Archiv).")
            }

            val db = dbFile
                ?: return ImportResult(0, errorMessage = "Im Backup wurde keine nova.db gefunden.")

            val result = SQLiteDatabase.openDatabase(db.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { sqlite ->
                NovaBackupParser.parse(AndroidNovaDb(sqlite), xml)
            }
            if (result.icons.isEmpty()) {
                return ImportResult(0, result.iconPackPackage, "Im Backup sind keine benutzerdefinierten Icons gespeichert.")
            }
            val count = store.replaceAll(result.icons)
            return ImportResult(
                importedCount = count,
                iconPackPackage = result.iconPackPackage,
                favoriteComponents = result.favoriteComponents,
                customLabels = result.customLabels
            )
        } catch (e: Exception) {
            return ImportResult(0, errorMessage = "Import fehlgeschlagen: ${e.javaClass.simpleName}: ${e.message}")
        } finally {
            workDir.deleteRecursively()
        }
    }

    private class AndroidNovaDb(private val db: SQLiteDatabase) : NovaDb {
        override fun query(sql: String): List<List<Any?>> {
            val rows = ArrayList<List<Any?>>()
            db.rawQuery(sql, null).use { c ->
                while (c.moveToNext()) {
                    rows.add(List(c.columnCount) { i -> c.valueAt(i) })
                }
            }
            return rows
        }

        private fun Cursor.valueAt(i: Int): Any? = when (getType(i)) {
            Cursor.FIELD_TYPE_NULL -> null
            Cursor.FIELD_TYPE_INTEGER -> getLong(i)
            Cursor.FIELD_TYPE_FLOAT -> getDouble(i)
            Cursor.FIELD_TYPE_BLOB -> getBlob(i)
            else -> getString(i)
        }
    }
}
