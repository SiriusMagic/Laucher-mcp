package com.zirakusa.launcher.data

/**
 * The cards «Hoy» knows. Family cards come from an app's summary ([pkg]); while that app is missing or does not give a
 * summary yet, its card shows as a preview of what is coming. [FREQUENT] and [RECENT] are Inicio's own.
 */
enum class CardKind(val id: String, val pkg: String?) {
    CLOCK("clock", "com.zirakusa.reloj"),
    CALENDAR("calendar", "com.zirakusa.calendario"),
    NOTES("notes", "com.zirakusa.notas"),
    DOCUMENTS("documents", "com.zirakusa.filem"),
    FREQUENT("frequent", null),
    RECENT("recent", null);
    companion object { fun of(id: String) = entries.firstOrNull { it.id == id } }
}

/** One place in «Hoy»: which card and whether it shows. */
data class Slot(val id: String, val visible: Boolean = true)

/** How a card is right now: with live lines, or a preview (its app is missing or has no summary yet). */
enum class CardStatus { LIVE, PREVIEW }

/** «Editar esta vista»: the order and visibility of the cards. Pure, with tests. */
object Cards {
    /** Other family apps with a summary get a card too: «app:<package>». */
    fun appId(pkg: String) = "app:$pkg"
    fun pkgOf(id: String): String? = CardKind.of(id)?.pkg ?: id.removePrefix("app:").takeIf { id.startsWith("app:") }

    /** Every card there can be: the known ones, then any other family app that gives a summary. */
    fun ids(summaryPackages: Collection<String>): List<String> {
        val known = CardKind.entries.mapNotNull { it.pkg }.toSet()
        return CardKind.entries.map { it.id } + summaryPackages.filter { it !in known }.distinct().sorted().map(::appId)
    }

    /** The saved order, without cards that no longer exist, and new cards at the end (shown). */
    fun merge(saved: List<Slot>, ids: List<String>): List<Slot> =
        saved.filter { it.id in ids }.distinctBy { it.id } + ids.filter { id -> saved.none { it.id == id } }.map { Slot(it) }

    /** The cards for the apps on the phone, in the saved order. */
    fun layout(saved: List<Slot>, apps: List<AppEntry>) = merge(saved, ids(apps.filter { it.summary != null }.map { it.pkg }))

    fun show(slots: List<Slot>, id: String, visible: Boolean) = slots.map { if (it.id == id) it.copy(visible = visible) else it }

    fun move(slots: List<Slot>, id: String, to: Int): List<Slot> {
        val slot = slots.firstOrNull { it.id == id } ?: return slots
        val rest = slots - slot
        return rest.toMutableList().apply { add(to.coerceIn(0, rest.size), slot) }
    }

    fun serialize(slots: List<Slot>) = slots.joinToString("\n") { "${it.id}\t${if (it.visible) 1 else 0}" }
    fun parse(text: String?): List<Slot> = text.orEmpty().lines().mapNotNull { line ->
        val parts = line.split('\t')
        if (parts.size != 2 || parts[0].isBlank()) null else Slot(parts[0], parts[1] != "0")
    }

    /** Live when the card's app answered with a summary (Inicio's own cards are always live). */
    fun status(id: String, livePackages: Set<String>): CardStatus {
        val pkg = pkgOf(id) ?: return CardStatus.LIVE
        return if (pkg in livePackages) CardStatus.LIVE else CardStatus.PREVIEW
    }
}
