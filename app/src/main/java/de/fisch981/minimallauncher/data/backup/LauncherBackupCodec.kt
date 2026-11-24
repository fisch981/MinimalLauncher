package de.fisch981.minimallauncher.data.backup

import de.fisch981.minimallauncher.data.model.HomeGestureAction

data class LauncherBackupData(
    val format: String = LauncherBackupCodec.FORMAT_ID,
    val version: Int = LauncherBackupCodec.CURRENT_VERSION,
    val favorites: List<String> = emptyList(),
    val hiddenApps: Set<String> = emptySet(),
    val popupFolders: Map<String, List<String>> = emptyMap(),
    val scrollerSide: String = "RIGHT",
    val waveIdleAlphaPercent: Int = 40,
    val appLanguage: String = "system",
    val appFont: String = "system",
    val clockDateFont: String = "system",
    val waveFont: String = "system",
    val appFontSizeSp: Int = 17,
    val appRowSpacingDp: Int = 8,
    val dateSizeSp: Int = 15,
    val waveFontSizeSp: Int = 11,
    val appLongPressDelayMs: Int = 500,
    val showClock: Boolean = true,
    val clockSizeSp: Int = 52,
    val showDate: Boolean = true,
    val showMedia: Boolean = true,
    val showBattery: Boolean = true,
    val showNextAlarm: Boolean = true,
    val showRecentApps: Boolean = true,
    val showAppIcons: Boolean = true,
    val hideStatusBar: Boolean = false,
    val showBottomActions: Boolean = true,
    val showNotificationApps: Boolean = true,
    val showNotificationPreview: Boolean = true,
    val showNotificationCount: Boolean = true,
    val homeSwipeDownAction: String = HomeGestureAction.EXPAND_NOTIFICATIONS.code,
    val homeSwipeUpAction: String = HomeGestureAction.SEARCH.code,
    val homeDoubleTapAction: String = HomeGestureAction.NONE.code,
    val recentApps: List<String> = emptyList(),
    val leftWaveCount: Int = 0,
    val rightWaveCount: Int = 0,
    val appLaunchCounts: Map<String, Int> = emptyMap(),
    val customIcons: Map<String, String> = emptyMap(),
    val customNames: Map<String, String> = emptyMap(),
    val importedIconFiles: Map<String, String> = emptyMap()
)

object LauncherBackupCodec {

    const val FORMAT_ID = "minimallauncher-backup"
    const val CURRENT_VERSION = 1

    fun encodeToJson(data: LauncherBackupData): String {
        val sb = StringBuilder(2048)
        sb.append("{\n")
        appendField(sb, "format", data.format, indent = 2, trailingComma = true)
        appendIntField(sb, "version", data.version, indent = 2, trailingComma = true)
        appendStringListField(sb, "favorites", data.favorites, indent = 2, trailingComma = true)
        appendStringListField(sb, "hiddenApps", data.hiddenApps.toList(), indent = 2, trailingComma = true)
        appendStringListMapField(sb, "popupFolders", data.popupFolders, indent = 2, trailingComma = true)
        appendField(sb, "scrollerSide", data.scrollerSide, indent = 2, trailingComma = true)
        appendIntField(sb, "waveIdleAlphaPercent", data.waveIdleAlphaPercent, indent = 2, trailingComma = true)
        appendField(sb, "appLanguage", data.appLanguage, indent = 2, trailingComma = true)
        appendField(sb, "appFont", data.appFont, indent = 2, trailingComma = true)
        appendField(sb, "clockDateFont", data.clockDateFont, indent = 2, trailingComma = true)
        appendField(sb, "waveFont", data.waveFont, indent = 2, trailingComma = true)
        appendIntField(sb, "appFontSizeSp", data.appFontSizeSp, indent = 2, trailingComma = true)
        appendIntField(sb, "appRowSpacingDp", data.appRowSpacingDp, indent = 2, trailingComma = true)
        appendIntField(sb, "dateSizeSp", data.dateSizeSp, indent = 2, trailingComma = true)
        appendIntField(sb, "waveFontSizeSp", data.waveFontSizeSp, indent = 2, trailingComma = true)
        appendIntField(sb, "appLongPressDelayMs", data.appLongPressDelayMs, indent = 2, trailingComma = true)
        appendBoolField(sb, "showClock", data.showClock, indent = 2, trailingComma = true)
        appendIntField(sb, "clockSizeSp", data.clockSizeSp, indent = 2, trailingComma = true)
        appendBoolField(sb, "showDate", data.showDate, indent = 2, trailingComma = true)
        appendBoolField(sb, "showMedia", data.showMedia, indent = 2, trailingComma = true)
        appendBoolField(sb, "showBattery", data.showBattery, indent = 2, trailingComma = true)
        appendBoolField(sb, "showNextAlarm", data.showNextAlarm, indent = 2, trailingComma = true)
        appendBoolField(sb, "showRecentApps", data.showRecentApps, indent = 2, trailingComma = true)
        appendBoolField(sb, "showAppIcons", data.showAppIcons, indent = 2, trailingComma = true)
        appendBoolField(sb, "hideStatusBar", data.hideStatusBar, indent = 2, trailingComma = true)
        appendBoolField(sb, "showBottomActions", data.showBottomActions, indent = 2, trailingComma = true)
        appendBoolField(sb, "showNotificationApps", data.showNotificationApps, indent = 2, trailingComma = true)
        appendBoolField(sb, "showNotificationPreview", data.showNotificationPreview, indent = 2, trailingComma = true)
        appendBoolField(sb, "showNotificationCount", data.showNotificationCount, indent = 2, trailingComma = true)
        appendField(sb, "homeSwipeDownAction", data.homeSwipeDownAction, indent = 2, trailingComma = true)
        appendField(sb, "homeSwipeUpAction", data.homeSwipeUpAction, indent = 2, trailingComma = true)
        appendField(sb, "homeDoubleTapAction", data.homeDoubleTapAction, indent = 2, trailingComma = true)
        appendStringListField(sb, "recentApps", data.recentApps, indent = 2, trailingComma = true)
        appendIntField(sb, "leftWaveCount", data.leftWaveCount, indent = 2, trailingComma = true)
        appendIntField(sb, "rightWaveCount", data.rightWaveCount, indent = 2, trailingComma = true)
        appendStringIntMapField(sb, "appLaunchCounts", data.appLaunchCounts, indent = 2, trailingComma = true)
        appendStringMapField(sb, "customIcons", data.customIcons, indent = 2, trailingComma = true)
        appendStringMapField(sb, "customNames", data.customNames, indent = 2, trailingComma = true)
        appendStringMapField(sb, "importedIconFiles", data.importedIconFiles, indent = 2, trailingComma = false)
        sb.append("}\n")
        return sb.toString()
    }

    fun decodeFromJson(json: String): LauncherBackupData {
        val root = JsonReader(json).parseRootObject()
        val format = root["format"] as? String
        require(format == FORMAT_ID) {
            "Invalid backup format"
        }
        val version = (root["version"] as? Number)?.toInt() ?: CURRENT_VERSION
        require(version in 1..CURRENT_VERSION) {
            "Unsupported backup version: $version"
        }

        return LauncherBackupData(
            format = format,
            version = version,
            favorites = asStringList(root["favorites"]),
            hiddenApps = asStringList(root["hiddenApps"]).toSet(),
            popupFolders = asStringListMap(root["popupFolders"]),
            scrollerSide = (root["scrollerSide"] as? String)?.takeIf { it.isNotBlank() } ?: "RIGHT",
            waveIdleAlphaPercent = ((root["waveIdleAlphaPercent"] as? Number)?.toInt() ?: 40).coerceIn(0, 100),
            appLanguage = (root["appLanguage"] as? String)?.takeIf { it.isNotBlank() } ?: "system",
            appFont = (root["appFont"] as? String)?.takeIf { it.isNotBlank() } ?: "system",
            clockDateFont = (root["clockDateFont"] as? String)?.takeIf { it.isNotBlank() } ?: "system",
            waveFont = (root["waveFont"] as? String)?.takeIf { it.isNotBlank() } ?: "system",
            appFontSizeSp = ((root["appFontSizeSp"] as? Number)?.toInt() ?: 17).coerceIn(13, 24),
            appRowSpacingDp = ((root["appRowSpacingDp"] as? Number)?.toInt() ?: 8).coerceIn(2, 18),
            dateSizeSp = ((root["dateSizeSp"] as? Number)?.toInt() ?: 15).coerceIn(11, 24),
            waveFontSizeSp = ((root["waveFontSizeSp"] as? Number)?.toInt() ?: 11).coerceIn(9, 16),
            appLongPressDelayMs = ((root["appLongPressDelayMs"] as? Number)?.toInt() ?: 500).coerceIn(250, 2000),
            showClock = (root["showClock"] as? Boolean) ?: true,
            clockSizeSp = ((root["clockSizeSp"] as? Number)?.toInt() ?: 52).coerceIn(28, 84),
            showDate = (root["showDate"] as? Boolean) ?: true,
            showMedia = (root["showMedia"] as? Boolean) ?: true,
            showBattery = (root["showBattery"] as? Boolean) ?: true,
            showNextAlarm = (root["showNextAlarm"] as? Boolean) ?: true,
            showRecentApps = (root["showRecentApps"] as? Boolean) ?: true,
            showAppIcons = (root["showAppIcons"] as? Boolean) ?: true,
            hideStatusBar = (root["hideStatusBar"] as? Boolean) ?: false,
            showBottomActions = (root["showBottomActions"] as? Boolean) ?: true,
            showNotificationApps = (root["showNotificationApps"] as? Boolean) ?: true,
            showNotificationPreview = (root["showNotificationPreview"] as? Boolean) ?: true,
            showNotificationCount = (root["showNotificationCount"] as? Boolean) ?: true,
            homeSwipeDownAction = HomeGestureAction.fromCode(
                root["homeSwipeDownAction"] as? String,
                HomeGestureAction.EXPAND_NOTIFICATIONS
            ).code,
            homeSwipeUpAction = HomeGestureAction.fromCode(
                root["homeSwipeUpAction"] as? String,
                HomeGestureAction.SEARCH
            ).code,
            homeDoubleTapAction = HomeGestureAction.fromCode(
                root["homeDoubleTapAction"] as? String,
                HomeGestureAction.NONE
            ).code,
            recentApps = asStringList(root["recentApps"]),
            leftWaveCount = ((root["leftWaveCount"] as? Number)?.toInt() ?: 0).coerceAtLeast(0),
            rightWaveCount = ((root["rightWaveCount"] as? Number)?.toInt() ?: 0).coerceAtLeast(0),
            appLaunchCounts = asStringIntMap(root["appLaunchCounts"]),
            customIcons = asStringMap(root["customIcons"]),
            customNames = asStringMap(root["customNames"]),
            importedIconFiles = asStringMap(root["importedIconFiles"])
        )
    }

    private fun asStringList(value: Any?): List<String> {
        val list = value as? List<*> ?: return emptyList()
        return list.mapNotNull { it as? String }.filter { it.isNotBlank() }
    }

    private fun asStringMap(value: Any?): Map<String, String> {
        val map = value as? Map<*, *> ?: return emptyMap()
        val result = LinkedHashMap<String, String>(map.size)
        for ((k, v) in map) {
            if (k is String && k.isNotBlank() && v is String && v.isNotBlank()) {
                result[k] = v
            }
        }
        return result
    }

    private fun asStringIntMap(value: Any?): Map<String, Int> {
        val map = value as? Map<*, *> ?: return emptyMap()
        val result = LinkedHashMap<String, Int>(map.size)
        for ((k, v) in map) {
            if (k is String && k.isNotBlank() && v is Number) {
                val n = v.toInt()
                if (n > 0) result[k] = n
            }
        }
        return result
    }

    private fun asStringListMap(value: Any?): Map<String, List<String>> {
        val map = value as? Map<*, *> ?: return emptyMap()
        val result = LinkedHashMap<String, List<String>>(map.size)
        for ((k, v) in map) {
            if (k is String && k.isNotBlank()) {
                val items = asStringList(v)
                if (items.isNotEmpty()) {
                    result[k] = items
                }
            }
        }
        return result
    }

    private fun appendField(sb: StringBuilder, key: String, value: String, indent: Int, trailingComma: Boolean) {
        appendIndent(sb, indent)
        appendJsonString(sb, key)
        sb.append(": ")
        appendJsonString(sb, value)
        if (trailingComma) sb.append(',')
        sb.append('\n')
    }

    private fun appendIntField(sb: StringBuilder, key: String, value: Int, indent: Int, trailingComma: Boolean) {
        appendIndent(sb, indent)
        appendJsonString(sb, key)
        sb.append(": ").append(value)
        if (trailingComma) sb.append(',')
        sb.append('\n')
    }

    private fun appendBoolField(sb: StringBuilder, key: String, value: Boolean, indent: Int, trailingComma: Boolean) {
        appendIndent(sb, indent)
        appendJsonString(sb, key)
        sb.append(": ").append(value)
        if (trailingComma) sb.append(',')
        sb.append('\n')
    }

    private fun appendStringListField(
        sb: StringBuilder,
        key: String,
        values: List<String>,
        indent: Int,
        trailingComma: Boolean
    ) {
        appendIndent(sb, indent)
        appendJsonString(sb, key)
        sb.append(": ")
        appendStringArray(sb, values, indent)
        if (trailingComma) sb.append(',')
        sb.append('\n')
    }

    private fun appendStringArray(sb: StringBuilder, values: List<String>, indent: Int) {
        if (values.isEmpty()) {
            sb.append("[]")
            return
        }
        sb.append("[\n")
        values.forEachIndexed { index, item ->
            appendIndent(sb, indent + 2)
            appendJsonString(sb, item)
            if (index < values.lastIndex) sb.append(',')
            sb.append('\n')
        }
        appendIndent(sb, indent)
        sb.append(']')
    }

    private fun appendStringMapField(
        sb: StringBuilder,
        key: String,
        map: Map<String, String>,
        indent: Int,
        trailingComma: Boolean
    ) {
        appendIndent(sb, indent)
        appendJsonString(sb, key)
        if (map.isEmpty()) {
            sb.append(": {}")
            if (trailingComma) sb.append(',')
            sb.append('\n')
            return
        }
        sb.append(": {\n")
        val entries = map.entries.toList()
        entries.forEachIndexed { index, (k, v) ->
            appendIndent(sb, indent + 2)
            appendJsonString(sb, k)
            sb.append(": ")
            appendJsonString(sb, v)
            if (index < entries.lastIndex) sb.append(',')
            sb.append('\n')
        }
        appendIndent(sb, indent)
        sb.append('}')
        if (trailingComma) sb.append(',')
        sb.append('\n')
    }

    private fun appendStringIntMapField(
        sb: StringBuilder,
        key: String,
        map: Map<String, Int>,
        indent: Int,
        trailingComma: Boolean
    ) {
        appendIndent(sb, indent)
        appendJsonString(sb, key)
        if (map.isEmpty()) {
            sb.append(": {}")
            if (trailingComma) sb.append(',')
            sb.append('\n')
            return
        }
        sb.append(": {\n")
        val entries = map.entries.toList()
        entries.forEachIndexed { index, (k, v) ->
            appendIndent(sb, indent + 2)
            appendJsonString(sb, k)
            sb.append(": ").append(v)
            if (index < entries.lastIndex) sb.append(',')
            sb.append('\n')
        }
        appendIndent(sb, indent)
        sb.append('}')
        if (trailingComma) sb.append(',')
        sb.append('\n')
    }

    private fun appendStringListMapField(
        sb: StringBuilder,
        key: String,
        map: Map<String, List<String>>,
        indent: Int,
        trailingComma: Boolean
    ) {
        appendIndent(sb, indent)
        appendJsonString(sb, key)
        if (map.isEmpty()) {
            sb.append(": {}")
            if (trailingComma) sb.append(',')
            sb.append('\n')
            return
        }
        sb.append(": {\n")
        val entries = map.entries.toList()
        entries.forEachIndexed { index, (k, list) ->
            appendIndent(sb, indent + 2)
            appendJsonString(sb, k)
            sb.append(": ")
            appendStringArray(sb, list, indent + 2)
            if (index < entries.lastIndex) sb.append(',')
            sb.append('\n')
        }
        appendIndent(sb, indent)
        sb.append('}')
        if (trailingComma) sb.append(',')
        sb.append('\n')
    }

    private fun appendIndent(sb: StringBuilder, spaces: Int) {
        repeat(spaces) { sb.append(' ') }
    }

    private fun appendJsonString(sb: StringBuilder, raw: String) {
        sb.append('"')
        for (ch in raw) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (ch.code < 0x20) {
                        sb.append("\\u")
                        sb.append(ch.code.toString(16).padStart(4, '0'))
                    } else {
                        sb.append(ch)
                    }
                }
            }
        }
        sb.append('"')
    }

    private class JsonReader(private val input: String) {
        private var pos = 0

        fun parseRootObject(): Map<String, Any?> {
            skipWhitespace()
            val obj = readObject()
            skipWhitespace()
            require(pos == input.length) { "Unexpected trailing content at index $pos" }
            return obj
        }

        private fun readValue(): Any? {
            skipWhitespace()
            require(pos < input.length) { "Unexpected end of JSON input" }
            return when (val c = input[pos]) {
                '{' -> readObject()
                '[' -> readArray()
                '"' -> readString()
                't' -> readLiteral("true", true)
                'f' -> readLiteral("false", false)
                'n' -> readLiteral("null", null)
                '-', in '0'..'9' -> readNumber()
                else -> throw IllegalArgumentException("Unexpected character '$c' at index $pos")
            }
        }

        private fun readObject(): Map<String, Any?> {
            expect('{')
            val map = LinkedHashMap<String, Any?>()
            skipWhitespace()
            if (peek('}')) {
                pos++
                return map
            }
            while (true) {
                skipWhitespace()
                val key = readString()
                skipWhitespace()
                expect(':')
                val value = readValue()
                map[key] = value
                skipWhitespace()
                if (peek('}')) {
                    pos++
                    return map
                }
                expect(',')
            }
        }

        private fun readArray(): List<Any?> {
            expect('[')
            val list = ArrayList<Any?>()
            skipWhitespace()
            if (peek(']')) {
                pos++
                return list
            }
            while (true) {
                list.add(readValue())
                skipWhitespace()
                if (peek(']')) {
                    pos++
                    return list
                }
                expect(',')
            }
        }

        private fun readString(): String {
            expect('"')
            val sb = StringBuilder()
            while (pos < input.length) {
                val c = input[pos++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        require(pos < input.length) { "Unterminated escape sequence" }
                        when (val esc = input[pos++]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                require(pos + 4 <= input.length) { "Invalid unicode escape at index $pos" }
                                val hex = input.substring(pos, pos + 4)
                                pos += 4
                                sb.append(hex.toInt(16).toChar())
                            }
                            else -> throw IllegalArgumentException("Invalid escape '\\$esc' at index ${pos - 1}")
                        }
                    }
                    else -> sb.append(c)
                }
            }
            throw IllegalArgumentException("Unterminated string")
        }

        private fun readNumber(): Number {
            val start = pos
            if (input[pos] == '-') pos++
            while (pos < input.length && input[pos] in '0'..'9') pos++
            var isFloat = false
            if (pos < input.length && input[pos] == '.') {
                isFloat = true
                pos++
                while (pos < input.length && input[pos] in '0'..'9') pos++
            }
            if (pos < input.length && (input[pos] == 'e' || input[pos] == 'E')) {
                isFloat = true
                pos++
                if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) pos++
                while (pos < input.length && input[pos] in '0'..'9') pos++
            }
            val token = input.substring(start, pos)
            return if (isFloat) token.toDouble() else token.toLong()
        }

        private fun <T> readLiteral(literal: String, result: T): T {
            require(input.startsWith(literal, pos)) { "Invalid token at index $pos" }
            pos += literal.length
            return result
        }

        private fun skipWhitespace() {
            while (pos < input.length) {
                when (input[pos]) {
                    ' ', '\t', '\r', '\n' -> pos++
                    else -> return
                }
            }
        }

        private fun expect(ch: Char) {
            require(pos < input.length && input[pos] == ch) {
                "Expected '$ch' at index $pos"
            }
            pos++
        }

        private fun peek(ch: Char): Boolean = pos < input.length && input[pos] == ch
    }
}
