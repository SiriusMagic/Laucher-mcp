package com.zirakusa.launcher.data

import androidx.core.content.edit

/** Where the settings live: the phone's preferences in the app, a map in the tests. */
interface KeyValues {
    fun get(key: String): String?
    fun put(key: String, value: String?)
}

class MapValues(private val map: MutableMap<String, String> = mutableMapOf()) : KeyValues {
    override fun get(key: String) = map[key]
    override fun put(key: String, value: String?) { if (value == null) map.remove(key) else map[key] = value }
}

/** Every setting of Inicio, typed. Unknown or broken values fall back to the defaults. */
class Settings(private val kv: KeyValues) {
    private fun flag(key: String, default: Boolean) = kv.get(key)?.let { it == "1" } ?: default
    private fun setFlag(key: String, on: Boolean) = kv.put(key, if (on) "1" else "0")
    private fun list(key: String) = kv.get(key)?.split('\n')?.filter { it.isNotBlank() }.orEmpty()

    /** The dock's apps (keys), in order. Null until it was filled the first time. */
    var dock: List<String>? get() = kv.get("dock")?.let { list("dock") }
        set(v) = kv.put("dock", v?.take(Dock.MAX)?.joinToString("\n"))
    var hidden: Set<String> get() = list("hidden").toSet(); set(v) = kv.put("hidden", v.sorted().joinToString("\n"))
    /** Apps per row in the drawer: 4 or 5. */
    var columns: Int get() = kv.get("columns")?.toIntOrNull()?.takeIf { it in COLUMNS } ?: 4; set(v) = kv.put("columns", v.toString())
    /** «Familia» (the apps that speak MCP) at the top of the drawer. */
    var familyFirst: Boolean get() = flag("family_first", true); set(v) = setFlag("family_first", v)
    /** The phone's wallpaper behind the home screen (otherwise the theme's colour). */
    var wallpaper: Boolean get() = flag("wallpaper", true); set(v) = setFlag("wallpaper", v)
    /** The clock and date on the home screen. */
    var clock: Boolean get() = flag("clock", true); set(v) = setFlag("clock", v)
    /** «Recientes» on top of the drawer. */
    var showRecent: Boolean get() = flag("show_recent", true); set(v) = setFlag("show_recent", v)
    /** The welcome steps were done (or skipped). */
    var onboarded: Boolean get() = flag("onboarded", false); set(v) = setFlag("onboarded", v)
    var counts: Map<String, Int> get() = Usage.parse(kv.get("counts")); set(v) = kv.put("counts", Usage.serialize(v))
    var recent: List<String> get() = list("recent"); set(v) = kv.put("recent", v.joinToString("\n"))
    /** «Editar esta vista»: the cards of «Hoy», in order, shown or not. */
    var layout: List<Slot> get() = Cards.parse(kv.get("hoy")); set(v) = kv.put("hoy", Cards.serialize(v))
    /** «Diseño»: Clásico or Lista. */
    var style: HomeStyle get() = kv.get("style")?.let { runCatching { HomeStyle.valueOf(it) }.getOrNull() } ?: HomeStyle.CLASSIC
        set(v) = kv.put("style", v.name)
    /** «Vista previa del calendario»: today's first event under the clock. */
    var calendarPreview: Boolean get() = flag("calendar_preview", true); set(v) = setFlag("calendar_preview", v)
    /** «Sugerir apps frecuentes» in the «Lista» favourites. */
    var suggest: Boolean get() = flag("suggest", true); set(v) = setFlag("suggest", v)
    /** «Ordenar según su uso» inside each letter of the drawer. */
    var sortByUsage: Boolean get() = flag("sort_usage", false); set(v) = setFlag("sort_usage", v)
    var theme: String? get() = kv.get("theme"); set(v) = kv.put("theme", v)
    var accent: String? get() = kv.get("accent"); set(v) = kv.put("accent", v)

    companion object { val COLUMNS = listOf(4, 5) }
}

/** The phone's preferences file «inicio» (kept out of backups). The MCP tools read the same. */
class SharedValues(context: android.content.Context) : KeyValues {
    private val prefs = context.applicationContext.getSharedPreferences("inicio", android.content.Context.MODE_PRIVATE)
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun put(key: String, value: String?) {
        prefs.edit(commit = true) { if (value == null) remove(key) else putString(key, value) }
    }
}
