package com.zirakusa.launcher

import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.Apps
import com.zirakusa.launcher.data.Dock
import com.zirakusa.launcher.data.MapValues
import com.zirakusa.launcher.data.Settings
import com.zirakusa.launcher.ui.HomeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The pure rules (order, letters, search, family, dock) and how the state keeps the dock and hidden apps. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LauncherTest {
    private fun app(label: String, pkg: String, module: String? = null) = AppEntry(pkg, "$pkg.Main", label, module)
    private val camara = app("Cámara", "com.android.camera")
    private val reloj = app("Reloj", "com.zirakusa.reloj", "clock_")
    private val notas = app("Notas", "com.zirakusa.notas", "notes_")
    private val chrome = app("Chrome", "com.android.chrome")
    private val nine = app("9GAG", "com.ninegag")
    private val apps = listOf(reloj, camara, nine, notas, chrome)

    @Test fun orderLettersAndSearch() {
        assertEquals(listOf("9GAG", "Cámara", "Chrome", "Notas", "Reloj"), Apps.sorted(apps).map { it.label })
        assertEquals('C', Apps.letter(camara)); assertEquals('#', Apps.letter(nine))
        assertEquals(listOf('C', 'N', 'R', '#'), Apps.groups(apps).map { it.first })
        assertEquals(listOf(camara), Apps.search(apps, "camara"))
        assertEquals(listOf(chrome), Apps.search(apps, "chr"))
        assertEquals(listOf(camara, chrome), Apps.search(apps, "c").take(2))                    // names that start with it first
        assertEquals(listOf(reloj, notas), Apps.search(apps, "zirakusa").sortedByDescending { it.label })
        assertTrue(Apps.search(apps, "  ").isEmpty())
        assertEquals(listOf(notas, reloj), Apps.family(apps))
        assertEquals(listOf(camara, chrome), Apps.visible(apps, setOf(nine.key, notas.key, reloj.key)))
        assertEquals(reloj, Apps.byPackage(apps, "com.zirakusa.reloj")); assertNull(Apps.byPackage(apps, "x.y"))
    }

    @Test fun dockRules() {
        var d = Dock.add(emptyList(), "a")
        d = Dock.add(d, "a"); assertEquals(listOf("a"), d)
        d = listOf("b", "c", "d", "e").fold(d) { acc, k -> Dock.add(acc, k) }
        assertEquals(5, d.size); assertEquals(d, Dock.add(d, "f"))                       // full
        assertEquals(listOf("e", "a", "b", "c", "d"), Dock.move(d, "e", 0))
        assertEquals(listOf("a", "b", "d", "e", "c"), Dock.move(d, "c", 99))
        assertEquals(listOf("a", "c"), Dock.clean(listOf("a", "x", "c"), setOf("a", "c")))
        assertEquals(listOf("a", "b"), Dock.seed(listOf("a", null, "b", "a")))
        assertEquals(4, Dock.seed(listOf("1", "2", "3", "4", "5", "6")).size)
    }

    @Test fun stateKeepsDockAndHidden() {
        val kv = MapValues()
        val state = HomeState(kv)
        state.load(apps) { listOf(chrome.key, camara.key, "gone/x") }
        assertEquals(listOf(chrome, camara), state.dockApps)                           // seeded once, unknown dropped
        state.load(apps) { listOf(reloj.key) }
        assertEquals(listOf(chrome, camara), state.dockApps)                           // not seeded again
        assertTrue(state.addToDock(reloj)); assertFalse(state.addToDock(reloj))
        state.hide(camara)
        assertEquals(listOf(chrome, reloj), state.dockApps); assertFalse(camara in state.drawer)
        assertEquals(listOf(camara), state.hiddenApps)
        state.query = "cam"; assertTrue(state.results.isEmpty())                       // hidden apps are not found
        state.unhide(camara); assertEquals(listOf(camara), state.results)
        state.load(apps - reloj)
        assertEquals(listOf(chrome), state.dockApps)                                   // uninstalled leaves the dock
        val again = HomeState(kv).also { it.load(apps) }
        assertEquals(listOf(chrome), again.dockApps)
        assertEquals(listOf(notas, reloj), again.family)
        val s = Settings(MapValues())
        assertNull(s.dock); assertEquals(4, s.columns); assertTrue(s.familyFirst); assertTrue(s.wallpaper)
        s.columns = 7; assertEquals(4, s.columns); s.columns = 5; assertEquals(5, s.columns)
    }
}
