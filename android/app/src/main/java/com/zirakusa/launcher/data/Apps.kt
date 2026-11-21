package com.zirakusa.launcher.data

import java.text.Normalizer

/**
 * An app the launcher can open. [key] («package/activity») names it everywhere (dock, hidden list). [module] is the
 * MCP module prefix a family app declares for Gateway Hub (`reloj` → «clock_»); null for other apps.
 */
data class AppEntry(val pkg: String, val activity: String, val label: String, val module: String? = null) {
    val key get() = "$pkg/$activity"
    val family get() = module != null
}

/** Pure rules over the app list: order, letters, search, family first, hidden apps. Unit tested. */
object Apps {
    /** Without accents or case: «camara» finds «Cámara». */
    fun fold(text: String) = Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase().trim()

    fun sorted(apps: List<AppEntry>) = apps.sortedWith(compareBy({ fold(it.label) }, { it.key }))

    /** The letter an app is listed under («Á» under A); digits and symbols under «#». */
    fun letter(app: AppEntry): Char = fold(app.label).firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar() ?: '#'

    /** Apps whose name (or package) has [query]; names that start with it first. Empty query: none. */
    fun search(apps: List<AppEntry>, query: String): List<AppEntry> {
        val q = fold(query)
        if (q.isEmpty()) return emptyList()
        return sorted(apps.filter { fold(it.label).contains(q) || it.pkg.lowercase().contains(q) })
            .sortedBy { a -> if (fold(a.label).startsWith(q)) 0 else if (fold(a.label).split(' ').any { it.startsWith(q) }) 1 else 2 }
    }

    /** What the drawer shows: no hidden apps, A to Z. */
    fun visible(apps: List<AppEntry>, hidden: Set<String>) = sorted(apps.filterNot { it.key in hidden })

    /** The family apps (they speak MCP), A to Z. */
    fun family(apps: List<AppEntry>) = sorted(apps.filter { it.family })

    /** A to Z in groups by letter, «#» last. */
    fun groups(apps: List<AppEntry>): List<Pair<Char, List<AppEntry>>> =
        sorted(apps).groupBy(::letter).toList().sortedBy { (c, _) -> if (c == '#') '￿' else c }

    /** The first app of a package (some have several entries). */
    fun byPackage(apps: List<AppEntry>, pkg: String) = sorted(apps).firstOrNull { it.pkg == pkg }
}

/** The dock: up to [MAX] apps at the bottom of the home screen, in the user's order. Pure. */
object Dock {
    const val MAX = 5

    /** Adds at the end; nothing changes when it is already there or the dock is full. */
    fun add(dock: List<String>, key: String): List<String> = if (key in dock || dock.size >= MAX) dock else dock + key
    fun remove(dock: List<String>, key: String) = dock - key
    fun move(dock: List<String>, key: String, to: Int): List<String> {
        if (key !in dock) return dock
        val rest = dock - key
        return rest.toMutableList().apply { add(to.coerceIn(0, rest.size), key) }
    }
    /** Drops apps that are no longer installed. */
    fun clean(dock: List<String>, installed: Set<String>) = dock.filter { it in installed }
    /** The first dock: the phone, messages, camera and browser the phone uses, without repeats. */
    fun seed(candidates: List<String?>) = candidates.filterNotNull().distinct().take(MAX - 1)
}
