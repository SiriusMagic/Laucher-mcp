package com.zirakusa.launcher

import com.zirakusa.launcher.data.IconShape
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
import com.zirakusa.launcher.data.FamilyCard
import java.util.concurrent.ConcurrentHashMap

/** The phone's apps: the ones with a launcher entry, family apps marked by the module they declare for Gateway Hub. */
object PhoneApps {
    const val HUB_ACTION = "com.zirakusa.gatewayhub.action.APP"
    const val HUB_MODULE = "com.zirakusa.gatewayhub.module"
    const val SUMMARY = "com.zirakusa.family.summary"
    private val icons = ConcurrentHashMap<String, ImageBitmap>()

    private fun query(pm: PackageManager, intent: Intent, flags: Int): List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
        else @Suppress("DEPRECATION") pm.queryIntentActivities(intent, flags)

    fun list(context: Context): List<AppEntry> = runCatching {
        val pm = context.packageManager
        val family = query(pm, Intent(HUB_ACTION), PackageManager.GET_META_DATA).mapNotNull { r -> r.activityInfo.metaData?.let { r.activityInfo.packageName to it } }
        val modules = family.mapNotNull { (p, m) -> m.getString(HUB_MODULE)?.let { p to it } }.toMap()
        // A summary authority is trusted only when it belongs to the same package that declares it.
        val summaries = family.mapNotNull { (p, m) -> m.getString(SUMMARY)?.takeIf { it.startsWith("$p.") }?.let { p to it } }.toMap()
        query(pm, Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { r -> AppEntry(r.activityInfo.packageName, r.activityInfo.name, r.loadLabel(pm).toString(), modules[r.activityInfo.packageName],
                summaries[r.activityInfo.packageName]) }
            .distinctBy { it.key }
    }.getOrDefault(emptyList())

    /** «Hoy»: what each family app with a summary says now (apps signed by another developer answer nothing). Slow: not on the main thread. */
    fun summaries(context: Context, apps: List<AppEntry>): List<FamilyCard> = apps.filter { it.summary != null }.distinctBy { it.pkg }.mapNotNull { app ->
        runCatching {
            val b = context.contentResolver.call("content://${app.summary}".toUri(), "summary", null, null) ?: return@runCatching null
            FamilyCard.of(app, b.getString("title"), b.getStringArrayList("lines"), b.getString("empty"))
        }.getOrNull()
    }

    /** The app's icon, kept in memory once drawn. */
    /**
     * The app's icon at [size] px. With an «Estilo de iconos» other than the system's, an adaptive icon's own layers
     * (background and foreground) are cut to that shape, so every icon looks alike; older icons are drawn as they are.
     */
    fun icon(context: Context, app: AppEntry, size: Int, shape: IconShape = IconShape.SYSTEM): ImageBitmap? {
        val key = "${app.key}|${shape.key}|$size"
        return icons[key] ?: runCatching {
            val d = context.packageManager.getActivityIcon(ComponentName(app.pkg, app.activity))
            if (shape == IconShape.SYSTEM || d !is android.graphics.drawable.AdaptiveIconDrawable) d.toBitmap(size, size).asImageBitmap()
            else shaped(d, size, shape).asImageBitmap()
        }.getOrNull()?.also { icons[key] = it }
    }

    /** An adaptive icon's layers (108 dp, the middle 72 dp visible) drawn inside [shape]. */
    private fun shaped(d: android.graphics.drawable.AdaptiveIconDrawable, size: Int, shape: IconShape): android.graphics.Bitmap {
        val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        val r = size * shape.corner
        canvas.clipPath(android.graphics.Path().apply { addRoundRect(0f, 0f, size.toFloat(), size.toFloat(), r, r, android.graphics.Path.Direction.CW) })
        val pad = size / 4
        listOfNotNull(d.background, d.foreground).forEach { layer -> layer.setBounds(-pad, -pad, size + pad, size + pad); layer.draw(canvas) }
        return bmp
    }

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
