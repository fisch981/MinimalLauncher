package de.fisch981.minimallauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import de.fisch981.minimallauncher.data.model.AppItem
import de.fisch981.minimallauncher.data.model.ShortcutItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val iconPackManager: IconPackManager
) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    @Volatile
    private var isCallbackRegistered = false

    private var refreshJob: Job? = null

    private val launcherAppsCallback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: UserHandle) {
            schedulePackageRefresh()
        }

        override fun onPackageRemoved(packageName: String, user: UserHandle) {
            schedulePackageRefresh()
        }

        override fun onPackageChanged(packageName: String, user: UserHandle) {
            schedulePackageRefresh()
        }

        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
            schedulePackageRefresh()
        }

        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
            schedulePackageRefresh()
        }
    }

    private fun schedulePackageRefresh() {
        refreshApps(debounceMs = PACKAGE_REFRESH_DEBOUNCE_MS)
    }

    fun seedInitialApps(cachedApps: List<AppItem>) {
        if (_installedApps.value.isEmpty() && cachedApps.isNotEmpty()) {
            _installedApps.value = cachedApps
        }
    }

    fun registerCallbackOnly() {
        if (!isCallbackRegistered) {
            launcherApps.registerCallback(launcherAppsCallback, Handler(Looper.getMainLooper()))
            isCallbackRegistered = true
        }
    }

    fun startListening() {
        registerCallbackOnly()
        refreshApps()
    }

    fun stopListening() {
        refreshJob?.cancel()
        refreshJob = null
        if (isCallbackRegistered) {
            launcherApps.unregisterCallback(launcherAppsCallback)
            isCallbackRegistered = false
        }
    }

    fun refreshApps(priorityKeys: Set<String> = emptySet(), debounceMs: Long = 0L) {
        refreshJob?.cancel()
        refreshJob = repositoryScope.launch {
            if (debounceMs > 0L) {
                delay(debounceMs)
            }
            val apps = loadInstalledAppsInternal(priorityKeys)
            _installedApps.value = apps
        }
    }

    private suspend fun loadInstalledAppsInternal(
        priorityKeys: Set<String> = emptySet()
    ): List<AppItem> = withContext(Dispatchers.IO) {
        iconPackManager.ensureInitialized()
        val rawEntries = mutableListOf<Pair<UserHandle, LauncherActivityInfo>>()
        val profiles = userManager.userProfiles

        for (profile in profiles) {
            val activities = launcherApps.getActivityList(null, profile)
            for (info in activities) {
                // Do not show the launcher itself in its own app drawer
                if (info.applicationInfo.packageName == context.packageName) continue
                rawEntries.add(profile to info)
            }
        }

        fun buildAppItem(profile: UserHandle, info: LauncherActivityInfo): AppItem {
            val label = info.label?.toString() ?: info.applicationInfo.packageName
            val packageName = info.applicationInfo.packageName
            val activityName = info.componentName.className
            val monoIcon = iconPackManager.getAppIcon(packageName, activityName) {
                try {
                    info.getIcon(0)
                } catch (_: Exception) {
                    null
                }
            }
            return AppItem(
                packageName = packageName,
                activityName = activityName,
                label = label,
                userHandle = profile,
                icon = monoIcon
            )
        }

        val builtByKey = HashMap<String, AppItem>(rawEntries.size)
        if (priorityKeys.isNotEmpty() && _installedApps.value.isEmpty()) {
            val priorityItems = mutableListOf<AppItem>()
            for ((profile, info) in rawEntries) {
                val pkg = info.applicationInfo.packageName
                val act = info.componentName.className
                val key = "$pkg/$act#${profile.hashCode()}"
                if (priorityKeys.contains(key)) {
                    val item = buildAppItem(profile, info)
                    builtByKey[key] = item
                    priorityItems.add(item)
                }
            }
            if (priorityItems.isNotEmpty() && _installedApps.value.isEmpty()) {
                _installedApps.value = priorityItems
            }
        }

        val appList = ArrayList<AppItem>(rawEntries.size)
        for ((profile, info) in rawEntries) {
            val pkg = info.applicationInfo.packageName
            val act = info.componentName.className
            val key = "$pkg/$act#${profile.hashCode()}"
            val item = builtByKey[key] ?: buildAppItem(profile, info)
            appList.add(item)
        }

        appList.sortedBy { it.label.lowercase() }
    }

    fun launchApp(app: AppItem, sourceBounds: Rect? = null) {
        try {
            val opts = if (sourceBounds != null) {
                val bundle = Bundle()
                bundle.putParcelable("android.intent.extra.ORIGINATING_BOUNDS", sourceBounds)
                bundle
            } else null

            launcherApps.startMainActivity(
                android.content.ComponentName(app.packageName, app.activityName),
                app.userHandle,
                sourceBounds,
                opts
            )
        } catch (_: Exception) {
            // Fallback: standard Intent launch
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            } catch (_: Exception) {}
        }
    }

    fun getAppShortcuts(app: AppItem): List<ShortcutItem> {
        val results = mutableListOf<ShortcutItem>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                val query = LauncherApps.ShortcutQuery().apply {
                    setPackage(app.packageName)
                    setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                    )
                }
                val shortcuts = launcherApps.getShortcuts(query, app.userHandle) ?: emptyList()
                for (s in shortcuts) {
                    val label = s.shortLabel?.toString() ?: s.longLabel?.toString() ?: s.id
                    val iconDrawable = try {
                        launcherApps.getShortcutIconDrawable(s, 0)
                    } catch (_: Exception) { null }
                    val monoIcon = iconPackManager.drawableToMonochromeBitmap(iconDrawable, isAlreadyLines = false)

                    results.add(
                        ShortcutItem(
                            id = s.id,
                            packageName = s.`package`,
                            shortLabel = label,
                            longLabel = s.longLabel?.toString(),
                            icon = monoIcon,
                            rawShortcutInfo = s
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        return results
    }

    fun launchShortcut(shortcut: ShortcutItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && shortcut.rawShortcutInfo != null) {
            try {
                launcherApps.startShortcut(shortcut.rawShortcutInfo, null, null)
            } catch (_: Exception) {}
        }
    }

    companion object {
        private const val PACKAGE_REFRESH_DEBOUNCE_MS = 400L
    }
}
