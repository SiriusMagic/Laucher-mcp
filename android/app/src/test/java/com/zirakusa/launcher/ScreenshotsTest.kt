package com.zirakusa.launcher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.FamilyCard
import com.zirakusa.launcher.data.HomeStyle
import com.zirakusa.launcher.ui.screens.Section
import com.zirakusa.launcher.ui.screens.SkeletonCard
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.zirakusa.launcher.data.MapValues
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.LauncherApp
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeTheme
import com.zirakusa.launcher.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "es-w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotsTest {
    @get:Rule val compose = createComposeRule()
    private val state by lazy { HomeState(MapValues()) { LocalDateTime.of(2026, 11, 21, 9, 41) } }
    private val opened = mutableListOf<String>()
    private val actions = AppActions(open = { opened += it.pkg; state.launched(it) }, cards = { list ->
        listOf(FamilyCard(list.first { it.pkg == "com.zirakusa.reloj" }, "Reloj", listOf("Próxima alarma: lun 6:02 a. m.", "Temporizador: quedan 04:12"), ""),
            FamilyCard(list.first { it.pkg == "com.zirakusa.calendario" }, "Hoy en tu calendario", listOf("09:00 Reunión de equipo", "Todo el día · Viaje a Cuenca"), ""))
    })
    private fun app(label: String, pkg: String, module: String? = null) = AppEntry(pkg, "$pkg.Main", label, module)
    private val apps = listOf(app("Reloj", "com.zirakusa.reloj", "clock_"), app("Calendario", "com.zirakusa.calendario", "calendar_"),
        app("Notas", "com.zirakusa.notas", "notes_"), app("Filem", "com.zirakusa.filem", "files_"), app("Stone", "com.zirakusa.stone", "music_"),
        app("Teléfono", "com.android.dialer"), app("Mensajes", "com.android.mms"), app("Cámara", "com.android.camera"), app("Chrome", "com.android.chrome"),
        app("Ajustes", "com.android.settings"), app("Galería", "com.android.gallery"), app("Mapas", "com.google.maps"), app("Música", "com.music"),
        app("WhatsApp", "com.whatsapp"), app("YouTube", "com.youtube"), app("Banco", "com.bank"), app("Clima", "com.weather"), app("Duolingo", "com.duolingo"))

    private fun shot(name: String) { compose.waitForIdle(); onView(isRoot()).captureRoboImage("build/screenshots/inicio-$name.png") }
    private fun show() {
        state.load(apps) { listOf("com.android.dialer/com.android.dialer.Main", "com.android.mms/com.android.mms.Main",
            "com.android.camera/com.android.camera.Main", "com.android.chrome/com.android.chrome.Main") }
        state.nextAlarm = LocalDateTime.of(2026, 11, 23, 6, 2).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        state.changeWallpaper(false)
        state.finishWelcome()
        compose.setContent { HomeTheme(state.themeMode, state.accent) { LauncherApp(state, actions, ticking = false) } }
    }

    @Test fun homeDrawerAndMenu() {
        show()
        // «Widgets» (the default): analog clock, round buttons, date card and the next alarm.
        compose.onNodeWithContentDescription("9:41").assertExists()
        compose.onNodeWithText("SÁBADO").assertExists()
        compose.onNodeWithText("Noviembre").assertExists()
        compose.onNodeWithText("6:02").assertExists()
        compose.onNodeWithText("LUN · A. M.").assertExists()
        shot("home")
        state.changeStyle(HomeStyle.CLASSIC)
        compose.onNodeWithText("9:41").assertExists()
        compose.onNodeWithText("Sábado, 21 de noviembre").assertExists()
        compose.onNodeWithText("lun 6:02 a. m.").assertExists()
        shot("home-classic")
        state.changeStyle(HomeStyle.WIDGETS)
        compose.onNodeWithContentDescription("Todas las apps").performClick()
        assertEquals(Page.DRAWER, state.page)
        compose.onNodeWithText("Familia").assertExists()
        shot("drawer")
        compose.onNodeWithText("Buscar apps").performTextInput("camara")
        compose.onNodeWithText("Cámara").performClick()
        assertEquals(listOf("com.android.camera"), opened)
        state.query = ""
        state.menuFor = apps[1]
        compose.onNodeWithText("Añadir al dock").assertExists()
        shot("menu")
        compose.onNodeWithText("Añadir al dock").performClick()
        assertTrue("com.zirakusa.calendario/com.zirakusa.calendario.Main" in state.dock)
        state.menuFor = apps[13]
        compose.onNodeWithText("Ocultar").performClick()
        assertTrue(state.hidden.isNotEmpty())
        state.home()
        shot("home-dock")
    }

    @Test fun settingsHiddenMcpAndLight() {
        show()
        state.page = Page.SETTINGS
        compose.onNodeWithText("Modo MCP").assertExists()
        shot("settings-menu")
        compose.onNodeWithText("Cajón de apps").performClick()
        compose.onNodeWithText("5").performClick()
        assertEquals(5, state.columns)
        shot("settings")
        state.hide(apps[16])
        compose.onNodeWithText("Apps ocultas").performClick()
        compose.onNodeWithText("Clima").assertExists()
        shot("hidden")
        compose.onNodeWithText("Mostrar").performClick()
        assertTrue(state.hidden.isEmpty())
        state.page = Page.MCP
        compose.onNodeWithText("Redireccionar a Hub").assertExists()
        shot("mcp")
        state.changeTheme(ThemeMode.LIGHT)
        state.openDrawer()
        shot("drawer-light")
    }

    @Test fun welcomeAndToday() {
        state.load(apps) { listOf("com.android.dialer/com.android.dialer.Main", "com.android.camera/com.android.camera.Main") }
        state.changeWallpaper(false)
        compose.setContent { HomeTheme(state.themeMode, state.accent) { LauncherApp(state, actions, ticking = false) } }
        compose.onNodeWithText("Bienvenido a Inicio").assertExists()
        shot("welcome")
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("Reloj").performClick()
        shot("welcome-favourites")
        compose.onNodeWithText("Continuar").performClick(); compose.onNodeWithText("Continuar").performClick()
        state.isDefault = false
        shot("welcome-default")
        compose.onNodeWithText("¡Vamos!").performClick()
        assertEquals(Page.HOME, state.page); assertEquals(3, state.dock.size)
        listOf(8, 8, 8, 9, 13).forEach { actions.open(apps[it]) }
        state.page = Page.FEED
        compose.onNodeWithText("Buenos días").assertExists()
        compose.onNodeWithText("09:00 Reunión de equipo").assertExists()
        shot("today")
        state.page = Page.CARDS
        compose.onNodeWithText("Documentos").assertExists()
        shot("cards")
        state.showCard("frequent", false); state.showCard("recent", false); state.moveCard("documents", -3)
        state.page = Page.FEED
        compose.onNodeWithText("Documentos").assertExists()
        shot("today-preview")
        state.openDrawer()
        compose.onNodeWithText("Recientes").assertExists()
        shot("drawer-recent")
    }

    @Test fun listDesign() {
        show()
        state.changeStyle(HomeStyle.LIST)
        state.cards = listOf(FamilyCard(apps[1], "Hoy en tu calendario", listOf("09:00 Reunión de equipo"), ""))
        listOf(8, 8, 13, 14).forEach { actions.open(apps[it]) }
        compose.onNodeWithText("09:00 Reunión de equipo").assertExists()
        compose.onNodeWithText("Teléfono").assertExists()
        shot("list-home")
        state.openAt('M')
        compose.onNodeWithText("Mapas").assertExists()
        shot("list-drawer")
        state.page = Page.SETTINGS; state.section = Section.HOME
        compose.onNodeWithText("Lista").assertExists()
        shot("settings-home")
    }

    @Test fun skeletonWhileLoading() {
        compose.mainClock.autoAdvance = false
        compose.setContent { HomeTheme { androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) { repeat(3) { SkeletonCard() } } } }
        compose.mainClock.advanceTimeBy(500)
        onView(isRoot()).captureRoboImage("build/screenshots/inicio-today-loading.png")
    }
}
