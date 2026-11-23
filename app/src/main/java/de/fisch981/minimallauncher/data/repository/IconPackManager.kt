package de.fisch981.minimallauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import de.fisch981.minimallauncher.data.nova.ImportedIconStore
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

class IconPackManager(
    private val context: Context,
    private val importedIcons: ImportedIconStore
) {

    private val packageManager: PackageManager = context.packageManager
    private val iconPackComponentMap = mutableMapOf<String, String>()
    private val iconPackPackageFallbackMap = mutableMapOf<String, String>()
    private val allPackDrawables = LinkedHashSet<String>()
    private val customOverridesMap = mutableMapOf<String, String>()
    private val iconCache = ConcurrentHashMap<String, Bitmap>()
    @Volatile private var activeIconPackPackage: String? = null
    @Volatile private var activeIconPackResources: Resources? = null
    @Volatile private var isInitialized = false

    // Known Lines icon pack package names
    private val supportedLinesPacks = listOf(
        "com.natewren.lines",
        "com.natewren.linesfree",
        "com.natewren.linesdark"
    )

    fun setCustomOverrides(overrides: Map<String, String>) {
        customOverridesMap.clear()
        customOverridesMap.putAll(overrides)
        iconCache.clear()
    }

    fun clearIconCache() {
        iconCache.clear()
    }

    fun ensureInitialized() {
        if (!isInitialized) {
            initialize()
        }
    }

    @Synchronized
    fun initialize() {
        iconCache.clear()
        try {
            for (pkg in supportedLinesPacks) {
                if (isPackageInstalled(pkg) && loadIconPack(pkg)) {
                    return
                }
            }
            // Fallback: query installed theme packs via launcher theme intents
            val discovered = discoverInstalledIconPackPackages()
            val prioritized = discovered.sortedByDescending { it.contains("lines", ignoreCase = true) }
            for (pkg in prioritized) {
                if (loadIconPack(pkg)) {
                    return
                }
            }
        } finally {
            isInitialized = true
        }
    }

    private fun discoverInstalledIconPackPackages(): List<String> {
        val packages = LinkedHashSet<String>()
        val actions = listOf(
            "org.adw.launcher.THEMES",
            "org.adw.launcher.icons.ACTION_PICK_ICON",
            "com.novalauncher.THEME",
            "com.gau.go.launcherex.theme"
        )
        for (action in actions) {
            try {
                val infos = packageManager.queryIntentActivities(Intent(action), 0)
                for (info in infos) {
                    val pkg = info.activityInfo?.packageName
                    if (!pkg.isNullOrBlank()) packages.add(pkg)
                }
            } catch (_: Exception) {}
        }
        return packages.toList()
    }

    fun createExternalIconPickerIntent(): Intent? {
        val packActions = listOf(
            "org.adw.launcher.icons.ACTION_PICK_ICON",
            "com.novalauncher.THEME",
            "org.adw.launcher.THEMES",
            "com.gau.go.launcherex.theme"
        )
        val candidatePacks = LinkedHashSet<String>().apply {
            activeIconPackPackage?.let { add(it) }
            addAll(getInstalledIconPacks())
        }.sortedByDescending { it.contains("lines", ignoreCase = true) }

        for (pkg in candidatePacks) {
            for (action in packActions) {
                val intent = Intent(action).setPackage(pkg)
                val resolved = packageManager.queryIntentActivities(intent, 0).firstOrNull()
                if (resolved != null) {
                    return Intent(action).setClassName(
                        resolved.activityInfo.packageName,
                        resolved.activityInfo.name
                    )
                }
            }
            for (mime in listOf("image/*", null)) {
                for (action in listOf(Intent.ACTION_PICK, Intent.ACTION_GET_CONTENT)) {
                    val intent = Intent(action).setPackage(pkg).apply {
                        if (mime != null) type = mime
                    }
                    val resolved = packageManager.queryIntentActivities(intent, 0).firstOrNull()
                    if (resolved != null) {
                        return Intent(action).apply {
                            if (mime != null) type = mime
                            setClassName(resolved.activityInfo.packageName, resolved.activityInfo.name)
                        }
                    }
                }
            }
        }

        for (action in packActions) {
            val resolvedList = packageManager.queryIntentActivities(Intent(action), 0)
            val preferred = resolvedList
                .sortedByDescending { it.activityInfo.packageName.contains("lines", ignoreCase = true) }
                .firstOrNull()
            if (preferred != null) {
                return Intent(action).setClassName(
                    preferred.activityInfo.packageName,
                    preferred.activityInfo.name
                )
            }
        }
        return null
    }

    fun extractBitmapFromPickerResult(data: Intent?): Bitmap? {
        if (data == null) return null

        // 1. Returned Uri (content:// or android.resource://)
        data.data?.let { uri ->
            decodeBitmapFromUri(uri)?.let { return it }
        }

        // 2. Direct Bitmap extra ("icon", "data", EXTRA_SHORTCUT_ICON)
        for (extraKey in listOf("icon", "data", Intent.EXTRA_SHORTCUT_ICON)) {
            @Suppress("DEPRECATION")
            val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra(extraKey, Bitmap::class.java)
            } else {
                data.getParcelableExtra(extraKey)
            }
            if (bmp != null) return bmp
        }

        // 3. ShortcutIconResource
        @Suppress("DEPRECATION")
        val iconRes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource::class.java)
        } else {
            data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
        }
        if (iconRes != null) {
            try {
                val res = packageManager.getResourcesForApplication(iconRes.packageName)
                val resId = res.getIdentifier(iconRes.resourceName, null, iconRes.packageName)
                    .takeIf { it != 0 }
                    ?: res.getIdentifier(iconRes.resourceName, "drawable", iconRes.packageName)
                if (resId != 0) {
                    return drawableToMonochromeBitmap(res.getDrawable(resId, null), isAlreadyLines = true)
                }
            } catch (_: Exception) {}
        }

        // 4. Integer drawable resource ID from active pack
        val resIdExtra = data.extras?.get("icon") as? Int
        if (resIdExtra != null && resIdExtra != 0 && activeIconPackResources != null) {
            try {
                return drawableToMonochromeBitmap(
                    activeIconPackResources!!.getDrawable(resIdExtra, null),
                    isAlreadyLines = true
                )
            } catch (_: Exception) {}
        }

        // 5. String drawable name ("icon" or "drawable")
        for (key in listOf("icon", "drawable")) {
            val name = data.getStringExtra(key)
            if (!name.isNullOrBlank()) {
                getIconByName(name)?.let { return it }
            }
        }

        return null
    }

    private fun decodeBitmapFromUri(uri: Uri): Bitmap? {
        if (uri.scheme == "android.resource") {
            val pkg = uri.authority
            if (!pkg.isNullOrBlank()) {
                try {
                    val res = packageManager.getResourcesForApplication(pkg)
                    val segments = uri.pathSegments
                    val resId = when {
                        segments.size == 1 -> segments[0].toIntOrNull() ?: 0
                        segments.size >= 2 -> res.getIdentifier(segments[1], segments[0], pkg)
                        else -> 0
                    }
                    if (resId != 0) {
                        return drawableToMonochromeBitmap(res.getDrawable(resId, null), isAlreadyLines = true)
                    }
                } catch (_: Exception) {}
            }
        }
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun isLinesPackInstalled(): Boolean {
        return activeIconPackPackage != null ||
            supportedLinesPacks.any { isPackageInstalled(it) } ||
            discoverInstalledIconPackPackages().any { it.contains("lines", ignoreCase = true) }
    }

    fun getInstalledIconPacks(): List<String> {
        val found = LinkedHashSet<String>()
        for (pkg in supportedLinesPacks) {
            if (isPackageInstalled(pkg)) found.add(pkg)
        }
        found.addAll(discoverInstalledIconPackPackages())
        return found.toList()
    }

    fun loadIconPack(packageName: String): Boolean {
        try {
            val res = packageManager.getResourcesForApplication(packageName)
            val mapping = parseAppFilter(res, packageName)
            val drawables = parseDrawableXml(res, packageName)
            if (mapping.isNotEmpty() || drawables.isNotEmpty()) {
                activeIconPackPackage = packageName
                activeIconPackResources = res
                iconPackComponentMap.clear()
                iconPackComponentMap.putAll(mapping)
                iconPackPackageFallbackMap.clear()
                for ((component, drawable) in mapping) {
                    val pkg = component.substringBefore('/')
                    if (pkg.isNotBlank() && !iconPackPackageFallbackMap.containsKey(pkg)) {
                        iconPackPackageFallbackMap[pkg] = drawable
                    }
                }
                allPackDrawables.clear()
                allPackDrawables.addAll(drawables)
                allPackDrawables.addAll(mapping.values)
                return true
            }
        } catch (_: Exception) {}
        return false
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun parseAppFilter(resources: Resources, packPackage: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            var stream: InputStream? = null
            try {
                stream = resources.assets.open("appfilter.xml")
            } catch (_: Exception) {
                val xmlId = resources.getIdentifier("appfilter", "xml", packPackage)
                if (xmlId != 0) {
                    val parser = resources.getXml(xmlId)
                    return parseAppFilterXml(parser)
                }
            }

            if (stream != null) {
                stream.use { s ->
                    val factory = XmlPullParserFactory.newInstance()
                    val parser = factory.newPullParser()
                    parser.setInput(s, "UTF-8")
                    return parseAppFilterXml(parser)
                }
            }
        } catch (_: Exception) {}
        return map
    }

    private fun parseAppFilterXml(parser: XmlPullParser): Map<String, String> {
        val map = mutableMapOf<String, String>()
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val component = parser.getAttributeValue(null, "component")
                val drawable = parser.getAttributeValue(null, "drawable")
                if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                    val cleanComponent = component
                        .replace("ComponentInfo{", "")
                        .replace("}", "")
                    map[cleanComponent] = drawable
                }
            }
            eventType = parser.next()
        }
        return map
    }

    private fun parseDrawableXml(resources: Resources, packPackage: String): Set<String> {
        val set = LinkedHashSet<String>()
        try {
            var stream: InputStream? = null
            try {
                stream = resources.assets.open("drawable.xml")
            } catch (_: Exception) {
                val xmlId = resources.getIdentifier("drawable", "xml", packPackage)
                if (xmlId != 0) {
                    val parser = resources.getXml(xmlId)
                    parseDrawableItems(parser, set)
                    return set
                }
            }
            if (stream != null) {
                stream.use { s ->
                    val factory = XmlPullParserFactory.newInstance()
                    val parser = factory.newPullParser()
                    parser.setInput(s, "UTF-8")
                    parseDrawableItems(parser, set)
                }
            }
        } catch (_: Exception) {}
        return set
    }

    private fun parseDrawableItems(parser: XmlPullParser, target: MutableSet<String>) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val drawable = parser.getAttributeValue(null, "drawable")
                if (!drawable.isNullOrBlank()) {
                    target.add(drawable)
                }
            }
            eventType = parser.next()
        }
    }

    fun getAllAvailableDrawables(): List<String> {
        val list = LinkedHashSet<String>()
        list.addAll(allPackDrawables)
        list.addAll(iconPackComponentMap.values)
        list.addAll(customOverridesMap.values)
        list.addAll(importedIcons.listImportedDrawables())
        return list.filter { it.isNotBlank() }.sorted()
    }

    fun getIconByName(drawableName: String): Bitmap? {
        if (drawableName.startsWith("nova:")) {
            return importedIcons.loadDrawableByKey(drawableName)
        }
        if (activeIconPackResources != null && activeIconPackPackage != null) {
            try {
                val cleanName = drawableName.removeSuffix(".png").removeSuffix(".xml")
                val resId = activeIconPackResources!!.getIdentifier(
                    cleanName,
                    "drawable",
                    activeIconPackPackage
                )
                if (resId != 0) {
                    val packDrawable = activeIconPackResources!!.getDrawable(resId, null)
                    return drawableToMonochromeBitmap(packDrawable, isAlreadyLines = true)
                }
            } catch (_: Exception) {}
        }
        return importedIcons.loadDrawableByKey(drawableName)
    }

    fun getAppIcon(packageName: String, activityName: String, fallbackDrawable: Drawable?): Bitmap {
        return getAppIcon(packageName, activityName) { fallbackDrawable }
    }

    fun getAppIcon(
        packageName: String,
        activityName: String,
        fallbackDrawableProvider: () -> Drawable?
    ): Bitmap {
        val componentKey = "$packageName/$activityName"
        iconCache[componentKey]?.let { return it }

        val resolved = resolveAppIcon(componentKey, packageName, fallbackDrawableProvider)
        iconCache[componentKey] = resolved
        return resolved
    }

    private fun resolveAppIcon(
        componentKey: String,
        packageName: String,
        fallbackDrawableProvider: () -> Drawable?
    ): Bitmap {
        // 1. Priorität: Manuell im Icon-Picker gewähltes Icon
        val overrideDrawableName = customOverridesMap[componentKey] ?: customOverridesMap[packageName]
        if (overrideDrawableName != null) {
            getIconByName(overrideDrawableName)?.let { return it }
        }

        // 2. Priorität: Aus dem Nova-Backup importiertes Icon (exakt das Bild, das Nova angezeigt hat)
        importedIcons.load(componentKey)?.let { return it }
        importedIcons.loadForPackage(packageName)?.let { return it }

        // 3. Priorität: Lines-Pack laut appfilter.xml
        val drawableName = iconPackComponentMap[componentKey] ?: iconPackPackageFallbackMap[packageName]
        val packResources = activeIconPackResources
        val packPackage = activeIconPackPackage
        if (drawableName != null && packResources != null && packPackage != null) {
            try {
                val resId = packResources.getIdentifier(
                    drawableName,
                    "drawable",
                    packPackage
                )
                if (resId != 0) {
                    val packDrawable = packResources.getDrawable(resId, null)
                    return drawableToMonochromeBitmap(packDrawable, isAlreadyLines = true)
                }
            } catch (_: Exception) {}
        }

        // 4. Fallback: Original-App-Icon erst jetzt bei Bedarf laden und in Monochrom konvertieren
        return drawableToMonochromeBitmap(fallbackDrawableProvider(), isAlreadyLines = false)
    }

    /**
     * Transforms any drawable into a pure monochrome / high-contrast black & white bitmap
     */
    fun drawableToMonochromeBitmap(drawable: Drawable?, isAlreadyLines: Boolean = false): Bitmap {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (drawable == null) {
            return bitmap
        }

        if (isAlreadyLines) {
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
            return bitmap
        }

        val tempBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(tempCanvas)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)

        val contrast = 1.6f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(matrix)

        canvas.drawBitmap(tempBitmap, 0f, 0f, paint)
        tempBitmap.recycle()

        return bitmap
    }
}
