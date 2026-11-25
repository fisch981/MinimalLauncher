package de.fisch981.minimallauncher.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import de.fisch981.minimallauncher.data.model.AppItem
import de.fisch981.minimallauncher.ui.viewmodel.LauncherUiState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Synchroner Startup-Cache für Favoriten und deren Icons, sodass nach Cold-Start
 * oder längerem Standby der Homescreen ohne Verzögerung im ersten Frame erscheint.
 */
class StartupCache(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
    private val iconsDir = File(context.filesDir, ICONS_DIR_NAME)
    private val lock = Any()

    fun loadInitialUiState(): LauncherUiState {
        return try {
            val jsonStr = prefs.getString(KEY_FAVORITES_JSON, null)
            if (jsonStr.isNullOrBlank()) return LauncherUiState()

            val array = JSONArray(jsonStr)
            val favorites = ArrayList<AppItem>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                parseAppItem(obj, isFavorite = true)?.let { favorites.add(it) }
            }
            if (favorites.isEmpty()) return LauncherUiState()

            val sortedFavorites = favorites.sortedBy { it.label.lowercase() }
            val lettersRaw = prefs.getString(KEY_AVAILABLE_LETTERS, "") ?: ""
            val letters = lettersRaw.toSet()

            LauncherUiState(
                favoriteApps = sortedFavorites,
                allApps = sortedFavorites,
                availableLetters = letters
            )
        } catch (_: Exception) {
            LauncherUiState()
        }
    }

    fun saveSnapshot(state: LauncherUiState) {
        synchronized(lock) {
            try {
                if (state.favoriteApps.isEmpty()) {
                    iconsDir.listFiles()?.forEach { it.delete() }
                    prefs.edit()
                        .remove(KEY_FAVORITES_JSON)
                        .putString(KEY_AVAILABLE_LETTERS, state.availableLetters.joinToString(""))
                        .apply()
                    return
                }
                if (!iconsDir.exists()) {
                    iconsDir.mkdirs()
                }
                val activeFiles = HashSet<String>()
                val array = JSONArray()
                for (fav in state.favoriteApps) {
                    array.put(serializeAppItem(fav, activeFiles))
                }

                iconsDir.listFiles()?.forEach { file ->
                    if (!activeFiles.contains(file.name)) {
                        file.delete()
                    }
                }

                val lettersStr = state.availableLetters.joinToString("")
                prefs.edit()
                    .putString(KEY_FAVORITES_JSON, array.toString())
                    .putString(KEY_AVAILABLE_LETTERS, lettersStr)
                    .apply()
            } catch (_: Exception) {
            }
        }
    }

    private fun serializeAppItem(app: AppItem, activeFiles: MutableSet<String>): JSONObject {
        val obj = JSONObject()
        obj.put("pkg", app.packageName)
        obj.put("act", app.activityName)
        obj.put("lbl", app.label)
        obj.put("orig", app.originalLabel)
        val serial = try {
            userManager?.getSerialNumberForUser(app.userHandle) ?: -1L
        } catch (_: Exception) {
            -1L
        }
        obj.put("usr", serial)

        val iconFileName = iconFileName(app.packageName, app.activityName, app.userHandle)
        val bitmap = app.icon
        if (bitmap != null) {
            activeFiles.add(iconFileName)
            val file = File(iconsDir, iconFileName)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } catch (_: Exception) {
            }
        }

        if (app.subApps.isNotEmpty()) {
            val subArray = JSONArray()
            for (sub in app.subApps) {
                subArray.put(serializeAppItem(sub, activeFiles))
            }
            obj.put("subs", subArray)
        }
        return obj
    }

    private fun parseAppItem(obj: JSONObject, isFavorite: Boolean): AppItem? {
        val pkg = obj.optString("pkg", "")
        val act = obj.optString("act", "")
        val lbl = obj.optString("lbl", "")
        if (pkg.isBlank() || act.isBlank() || lbl.isBlank()) return null

        val orig = obj.optString("orig", lbl).ifBlank { lbl }
        val serial = obj.optLong("usr", -1L)
        val userHandle: UserHandle = try {
            if (serial >= 0L) {
                userManager?.getUserForSerialNumber(serial) ?: Process.myUserHandle()
            } else {
                Process.myUserHandle()
            }
        } catch (_: Exception) {
            Process.myUserHandle()
        }

        val iconFile = File(iconsDir, iconFileName(pkg, act, userHandle))
        val icon = if (iconFile.exists()) {
            try {
                BitmapFactory.decodeFile(iconFile.absolutePath)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }

        val subAppsList = mutableListOf<AppItem>()
        val subArray = obj.optJSONArray("subs")
        if (subArray != null) {
            for (i in 0 until subArray.length()) {
                val subObj = subArray.optJSONObject(i) ?: continue
                parseAppItem(subObj, isFavorite = false)?.let { subAppsList.add(it) }
            }
        }

        return AppItem(
            packageName = pkg,
            activityName = act,
            label = lbl,
            userHandle = userHandle,
            icon = icon,
            isFavorite = isFavorite,
            subApps = subAppsList,
            isHidden = false,
            originalLabel = orig
        )
    }

    private fun iconFileName(packageName: String, activityName: String, userHandle: UserHandle): String {
        val raw = "${packageName}_${activityName}_${userHandle.hashCode()}"
        val safe = raw.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return "$safe.png"
    }

    companion object {
        private const val PREFS_NAME = "minimal_launcher_startup_cache"
        private const val ICONS_DIR_NAME = "startup_fav_icons"
        private const val KEY_FAVORITES_JSON = "favorites_json"
        private const val KEY_AVAILABLE_LETTERS = "available_letters"

        data class SnapshotSignatureEntry(
            val packageName: String,
            val activityName: String,
            val label: String,
            val originalLabel: String,
            val userHash: Int,
            val iconIdentityHash: Int,
            val subApps: List<SnapshotSignatureEntry> = emptyList()
        )

        fun buildSnapshotSignature(state: LauncherUiState): String {
            return buildSnapshotSignatureFromEntries(
                availableLetters = state.availableLetters,
                favorites = state.favoriteApps.map { it.toSignatureEntry() }
            )
        }

        fun buildSnapshotSignatureFromEntries(
            availableLetters: Set<Char>,
            favorites: List<SnapshotSignatureEntry>
        ): String {
            val sb = StringBuilder()
            sb.append("letters=")
            availableLetters.sorted().forEach { sb.append(it) }
            sb.append("|favs=")
            favorites.forEach { appendSignatureEntry(sb, it) }
            return sb.toString()
        }

        private fun AppItem.toSignatureEntry(): SnapshotSignatureEntry {
            return SnapshotSignatureEntry(
                packageName = packageName,
                activityName = activityName,
                label = label,
                originalLabel = originalLabel,
                userHash = userHandle.hashCode(),
                iconIdentityHash = System.identityHashCode(icon),
                subApps = subApps.map { it.toSignatureEntry() }
            )
        }

        private fun appendSignatureEntry(sb: StringBuilder, entry: SnapshotSignatureEntry) {
            sb.append('[')
                .append(entry.packageName).append('/')
                .append(entry.activityName).append('#')
                .append(entry.userHash).append(':')
                .append(entry.label).append(':')
                .append(entry.originalLabel).append(':')
                .append(entry.iconIdentityHash)
            if (entry.subApps.isNotEmpty()) {
                sb.append("{")
                entry.subApps.forEach { appendSignatureEntry(sb, it) }
                sb.append("}")
            }
            sb.append(']')
        }
    }
}
