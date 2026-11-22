package com.zirakusa.launcher.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.Apps
import com.zirakusa.launcher.data.CardKind
import com.zirakusa.launcher.data.Cards
import com.zirakusa.launcher.data.HomeStyle
import com.zirakusa.launcher.data.ListStyle
import com.zirakusa.launcher.data.Dock
import com.zirakusa.launcher.data.Slot
import com.zirakusa.launcher.data.FamilyCard
import com.zirakusa.launcher.data.Greeting
import com.zirakusa.launcher.data.Usage
import com.zirakusa.launcher.data.KeyValues
import com.zirakusa.launcher.data.Settings
import com.zirakusa.launcher.ui.theme.Accent
import com.zirakusa.launcher.ui.theme.ThemeMode
import java.time.LocalDateTime

/** Where the user is. */
enum class Page { HOME, FEED, CARDS, DRAWER, SETTINGS, HIDDEN, MCP, WELCOME }

/**
 * The only owner of what the screens show: the phone's apps, the dock, the hidden apps, the drawer's search and the
 * settings. [clock] gives «now» (fixed in tests). The activity feeds [load]; the screens only read and call.
 */
class HomeState(values: KeyValues, private val clock: () -> LocalDateTime = LocalDateTime::now) {
    private val settings = Settings(values)

    var page by mutableStateOf(if (settings.onboarded) Page.HOME else Page.WELCOME)
    /** The step of the welcome (0 hello, 1 favourites, 2 wallpaper, 3 default). */
    var step by mutableIntStateOf(0)
    /** The favourites ticked in the welcome (they become the dock). */
    var picked by mutableStateOf(emptyList<String>())
    var counts by mutableStateOf(settings.counts); private set
    var recent by mutableStateOf(settings.recent); private set
    var showRecent by mutableStateOf(settings.showRecent); private set
    /** «Hoy»: the family apps' cards, read when «Hoy» opens. */
    var cards by mutableStateOf(emptyList<FamilyCard>())
    /** «Hoy»: the order of the cards and which show. */
    var slots by mutableStateOf(settings.layout); private set
    /** The category of «Ajustes» that is open. */
    var section by mutableStateOf(com.zirakusa.launcher.ui.screens.Section.MENU)
    /** The page «Editar esta vista» goes back to. */
    var cardsBack = Page.FEED
    var apps by mutableStateOf(emptyList<AppEntry>()); private set
    var dock by mutableStateOf(settings.dock.orEmpty()); private set
    var hidden by mutableStateOf(settings.hidden); private set
    var query by mutableStateOf("")
    /** The app whose menu is open (long press). */
    var menuFor by mutableStateOf<AppEntry?>(null)
    var now by mutableStateOf(clock()); private set
    /** The next alarm of any app (epoch ms), or null. */
    var nextAlarm by mutableStateOf<Long?>(null)
    var h24 by mutableStateOf(false)
    /** Inicio is the phone's home app. */
    var isDefault by mutableStateOf(true)
    /** Grows when the drawer should start at the top (opened again). */
    var drawerOpened by mutableIntStateOf(0); private set

    var columns by mutableIntStateOf(settings.columns); private set
    var style by mutableStateOf(settings.style); private set
    var calendarPreview by mutableStateOf(settings.calendarPreview); private set
    var suggest by mutableStateOf(settings.suggest); private set
    var sortByUsage by mutableStateOf(settings.sortByUsage); private set
    /** The letter the drawer should show first (from the alphabet on the home screen), or null. */
    var jumpTo by mutableStateOf<Char?>(null)
    var familyFirst by mutableStateOf(settings.familyFirst); private set
    var wallpaper by mutableStateOf(settings.wallpaper); private set
    var showClock by mutableStateOf(settings.clock); private set
    var themeMode by mutableStateOf(settings.theme?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.BLACK); private set
    var accent by mutableStateOf(settings.accent?.let { runCatching { Accent.valueOf(it) }.getOrNull() } ?: Accent.ROJO); private set

    fun tick() { now = clock() }

    /** The phone's apps changed (or were read for the first time). The first time, the dock is filled with [seed]. */
    fun load(list: List<AppEntry>, seed: () -> List<String?> = { emptyList() }) {
        apps = list
        val keys = list.map { it.key }.toSet()
        val saved = settings.dock ?: Dock.seed(seed()).also { settings.dock = it }
        dock = Dock.clean(saved, keys)
        if (dock != saved) settings.dock = dock
        if (page == Page.WELCOME && picked.isEmpty()) picked = dock
        slots = Cards.layout(settings.layout, list)
    }
    /** A tool changed «Hoy». */
    fun reloadCards() { slots = Cards.layout(settings.layout, apps) }

    /** The packages whose card is live (they answered with a summary). */
    val livePackages: Set<String> get() = cards.map { it.app.pkg }.toSet()
    fun showCard(id: String, visible: Boolean) = putSlots(Cards.show(slots, id, visible))
    fun moveCard(id: String, by: Int) = putSlots(Cards.move(slots, id, slots.indexOfFirst { it.id == id } + by))
    private fun putSlots(next: List<Slot>) { slots = next; settings.layout = next }
    fun editCards(from: Page) { cardsBack = from; page = Page.CARDS }
    /** A tool changed the dock. */
    fun reload() { dock = Dock.clean(settings.dock.orEmpty(), apps.map { it.key }.toSet()); hidden = settings.hidden; reloadCards() }

    private val byKey get() = apps.associateBy { it.key }
    val dockApps: List<AppEntry> get() = dock.mapNotNull { byKey[it] }
    val family: List<AppEntry> get() = Apps.family(apps).filterNot { it.key in hidden }
    val drawer: List<AppEntry> get() = Apps.visible(apps, hidden)
    val results: List<AppEntry> get() = Apps.search(apps.filterNot { it.key in hidden }, query)
    val hiddenApps: List<AppEntry> get() = Apps.sorted(apps.filter { it.key in hidden })

    val frequent: List<AppEntry> get() = Usage.frequent(counts, apps, hidden)
    val recentApps: List<AppEntry> get() = Usage.recent(recent, apps, hidden)
    val greeting: Greeting get() = Greeting.at(now.hour)

    /** The user opened [app] (counted on the phone only, for «Apps frecuentes» and «Recientes»). */
    fun launched(app: AppEntry) {
        val (c, r) = Usage.record(counts, recent, app.key)
        counts = c; recent = r; settings.counts = c; settings.recent = r
    }

    // Welcome
    fun togglePick(app: AppEntry) {
        picked = if (app.key in picked) picked - app.key else if (picked.size < Dock.MAX) picked + app.key else picked
    }
    fun nextStep() { if (step < 3) step++ else finishWelcome() }
    fun finishWelcome() { putDock(picked.take(Dock.MAX)); settings.onboarded = true; page = Page.HOME }

    fun openDrawer(search: Boolean = false) { query = ""; jumpTo = null; drawerOpened++; page = Page.DRAWER; searching = search }
    /** The drawer opened from «Buscar apps»: the keyboard comes up. */
    var searching by mutableStateOf(false)
    fun home() { query = ""; menuFor = null; page = Page.HOME }

    fun addToDock(app: AppEntry): Boolean = Dock.add(dock, app.key).let { next -> if (next == dock) false else { putDock(next); true } }
    fun removeFromDock(app: AppEntry) = putDock(Dock.remove(dock, app.key))
    fun moveInDock(app: AppEntry, to: Int) = putDock(Dock.move(dock, app.key, to))
    private fun putDock(next: List<String>) { dock = next; settings.dock = next }
    fun hide(app: AppEntry) { hidden = hidden + app.key; settings.hidden = hidden; if (app.key in dock) removeFromDock(app) }
    fun unhide(app: AppEntry) { hidden = hidden - app.key; settings.hidden = hidden }

    fun changeStyle(next: HomeStyle) { style = next; settings.style = next }
    fun changeCalendarPreview(on: Boolean) { calendarPreview = on; settings.calendarPreview = on }
    fun changeSuggest(on: Boolean) { suggest = on; settings.suggest = on }
    fun changeSortByUsage(on: Boolean) { sortByUsage = on; settings.sortByUsage = on }
    /** «Lista»: the favourites (dock first, then suggestions marked true). */
    val favourites: List<Pair<AppEntry, Boolean>> get() = ListStyle.favourites(dockApps, frequent, suggest)
    /** The drawer's apps in the order the settings ask. */
    val drawerOrdered: List<AppEntry> get() = if (sortByUsage) ListStyle.byUsage(drawer, counts) else drawer
    /** The first line of today's calendar card, for under the clock. */
    val calendarLine: String? get() = if (!calendarPreview) null else cards.firstOrNull { it.app.pkg == CardKind.CALENDAR.pkg }?.lines?.firstOrNull()
    /** The alphabet on the home screen: open the drawer at [letter]. */
    fun openAt(letter: Char) { openDrawer(); jumpTo = letter }

    fun changeShowRecent(on: Boolean) { showRecent = on; settings.showRecent = on }
    fun changeColumns(n: Int) { columns = n; settings.columns = n }
    fun changeFamilyFirst(on: Boolean) { familyFirst = on; settings.familyFirst = on }
    fun changeWallpaper(on: Boolean) { wallpaper = on; settings.wallpaper = on }
    fun changeClock(on: Boolean) { showClock = on; settings.clock = on }
    fun changeTheme(mode: ThemeMode) { themeMode = mode; settings.theme = mode.name }
    fun changeAccent(next: Accent) { accent = next; settings.accent = next.name }
}
