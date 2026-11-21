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
    private val actions = AppActions(open = { opened += it.pkg })
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
        compose.setContent { HomeTheme(state.themeMode, state.accent) { LauncherApp(state, actions, ticking = false) } }
    }

    @Test fun homeDrawerAndMenu() {
        show()
        compose.onNodeWithText("9:41").assertExists()
        compose.onNodeWithText("Sábado, 21 de noviembre").assertExists()
        compose.onNodeWithText("lun 6:02 a. m.").assertExists()
        shot("home")
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
}
