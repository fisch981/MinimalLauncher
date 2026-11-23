package de.fisch981.minimallauncher.data.nova

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.Base64

/**
 * Speichert die aus Nova importierten Icon-PNGs im App-Speicher.
 * Pro Komponente eine Datei (`c_<name>.png`), je Paket die erste gefundene (`p_<paket>.png`) als Fallback
 * sowie je Pack-Drawable (`d_<drawable>.png`) zur Auswahl im Icon-Picker.
 */
class ImportedIconStore(context: Context) {

    private val dir = File(context.filesDir, "nova_icons")

    fun replaceAll(icons: List<NovaIcon>): Int {
        dir.deleteRecursively()
        dir.mkdirs()
        var written = 0
        for (icon in icons) {
            File(dir, componentFile(icon.component)).writeBytes(icon.png)
            val pkgFile = File(dir, packageFile(icon.component.substringBefore('/')))
            if (!pkgFile.exists()) pkgFile.writeBytes(icon.png)
            val drawable = icon.packDrawable
            if (!drawable.isNullOrBlank()) {
                val drawFile = File(dir, drawableFile(drawable))
                if (!drawFile.exists()) drawFile.writeBytes(icon.png)
            }
            written++
        }
        return written
    }

    fun load(component: String): Bitmap? = decode(File(dir, componentFile(component)))

    fun loadForPackage(packageName: String): Bitmap? = decode(File(dir, packageFile(packageName)))

    fun loadDrawableByKey(key: String): Bitmap? {
        if (key.startsWith("nova:")) {
            return loadForPackage(key.removePrefix("nova:"))
        }
        return decode(File(dir, drawableFile(key)))
    }

    fun listImportedDrawables(): List<String> {
        val files = dir.listFiles() ?: return emptyList()
        val result = LinkedHashSet<String>()
        for (f in files) {
            when {
                f.name.startsWith("d_") && f.name.endsWith(".png") ->
                    result.add(f.name.removePrefix("d_").removeSuffix(".png"))
                f.name.startsWith("p_") && f.name.endsWith(".png") ->
                    result.add("nova:" + f.name.removePrefix("p_").removeSuffix(".png"))
            }
        }
        return result.toList().sorted()
    }

    fun removeForApp(component: String, packageName: String) {
        File(dir, componentFile(component)).delete()
        File(dir, packageFile(packageName)).delete()
    }

    fun saveBitmapForApp(component: String, packageName: String, bitmap: Bitmap): Boolean {
        return runCatching {
            dir.mkdirs()
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            val bytes = out.toByteArray()
            if (bytes.isEmpty()) return false
            File(dir, componentFile(component)).writeBytes(bytes)
            File(dir, packageFile(packageName)).writeBytes(bytes)
            true
        }.getOrDefault(false)
    }

    fun count(): Int = dir.listFiles { f -> f.name.startsWith("c_") }?.size ?: 0

    fun exportAllAsBase64(): Map<String, String> {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".png") } ?: return emptyMap()
        val encoder = Base64.getEncoder()
        val result = LinkedHashMap<String, String>(files.size)
        for (file in files.sortedBy { it.name }) {
            runCatching {
                val bytes = file.readBytes()
                if (bytes.isNotEmpty()) {
                    result[file.name] = encoder.encodeToString(bytes)
                }
            }
        }
        return result
    }

    fun importAllFromBase64(files: Map<String, String>) {
        dir.deleteRecursively()
        if (files.isEmpty()) return
        dir.mkdirs()
        val decoder = Base64.getDecoder()
        for ((name, encoded) in files) {
            if (!name.endsWith(".png") || name.contains('/') || name.contains('\\') || name.contains("..")) {
                continue
            }
            runCatching {
                val bytes = decoder.decode(encoded)
                if (bytes.isNotEmpty()) {
                    File(dir, name).writeBytes(bytes)
                }
            }
        }
    }

    fun clear() {
        dir.deleteRecursively()
    }

    private fun decode(file: File): Bitmap? =
        if (file.isFile) runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull() else null

    private fun componentFile(component: String) = "c_${safe(component)}.png"
    private fun packageFile(pkg: String) = "p_${safe(pkg)}.png"
    private fun drawableFile(drawable: String) = "d_${safe(drawable)}.png"

    private fun safe(name: String): String {
        val cleaned = name.replace(Regex("[^A-Za-z0-9._\$-]"), "_")
        if (cleaned.length <= 150) return cleaned
        val hash = MessageDigest.getInstance("SHA-1").digest(name.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return cleaned.take(100) + "_" + hash
    }
}
