package com.zirakusa.launcher

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.zirakusa.launcher.data.AppEntry
import java.util.concurrent.ConcurrentHashMap

/** The phone's apps: the ones with a launcher entry, family apps marked by the module they declare for Gateway Hub. */
object PhoneApps {
    const val HUB_ACTION = "com.zirakusa.gatewayhub.action.APP"
    const val HUB_MODULE = "com.zirakusa.gatewayhub.module"
    private val icons = ConcurrentHashMap<String, ImageBitmap>()

    private fun query(pm: PackageManager, intent: Intent, flags: Int): List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
        else @Suppress("DEPRECATION") pm.queryIntentActivities(intent, flags)

    fun list(context: Context): List<AppEntry> = runCatching {
        val pm = context.packageManager
        val modules = query(pm, Intent(HUB_ACTION), PackageManager.GET_META_DATA)
            .mapNotNull { r -> r.activityInfo.metaData?.getString(HUB_MODULE)?.let { r.activityInfo.packageName to it } }.toMap()
        query(pm, Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { r -> AppEntry(r.activityInfo.packageName, r.activityInfo.name, r.loadLabel(pm).toString(), modules[r.activityInfo.packageName]) }
            .distinctBy { it.key }
    }.getOrDefault(emptyList())

    /** The app's icon, kept in memory once drawn. */
    fun icon(context: Context, app: AppEntry, size: Int): ImageBitmap? = icons[app.key] ?: runCatching {
        context.packageManager.getActivityIcon(ComponentName(app.pkg, app.activity)).toBitmap(size, size).asImageBitmap()
    }.getOrNull()?.also { icons[app.key] = it }

    fun forget(pkg: String) { icons.keys.removeAll { it.startsWith("$pkg/") } }

    fun launch(app: AppEntry): Intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(ComponentName(app.pkg, app.activity)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

    fun info(pkg: String): Intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$pkg".toUri())

    fun uninstall(pkg: String): Intent = Intent(Intent.ACTION_DELETE, "package:$pkg".toUri())

    /** The first dock: the apps that answer a call, a message, a photo and a web page. */
    fun dockCandidates(context: Context): List<String?> {
        val pm = context.packageManager
        fun key(intent: Intent): String? {
            val r = query(pm, intent, PackageManager.MATCH_DEFAULT_ONLY).firstOrNull() ?: return null
            val launch = pm.getLaunchIntentForPackage(r.activityInfo.packageName)?.component ?: return null
            return "${launch.packageName}/${launch.className}"
        }
        return listOf(key(Intent(Intent.ACTION_DIAL)), key(Intent(Intent.ACTION_SENDTO, "smsto:".toUri())),
            key(Intent(MediaStore.ACTION_IMAGE_CAPTURE)), key(Intent(Intent.ACTION_VIEW, "https://example.com".toUri())))
    }

    /** When the next alarm of any app rings (epoch ms), or null. */
    fun nextAlarm(context: Context): Long? = context.getSystemService(AlarmManager::class.java)?.nextAlarmClock?.triggerTime

    /** Tapping the clock: our Reloj if installed, otherwise the phone's alarms. */
    fun clock(context: Context): Intent = context.packageManager.getLaunchIntentForPackage("com.zirakusa.reloj")
        ?: Intent(AlarmClock.ACTION_SHOW_ALARMS)

    /** Tapping the date: our Calendario if installed, otherwise the phone's calendar. */
    fun calendar(context: Context): Intent = context.packageManager.getLaunchIntentForPackage("com.zirakusa.calendario")
        ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
}
