package com.zirakusa.launcher

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.HomeStyle
import com.zirakusa.launcher.data.IconShape
import com.zirakusa.launcher.data.MapValues
import com.zirakusa.launcher.data.Pinch
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.EditSheet
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.LauncherApp
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDateTime

/** «Editar inicio»: a two-finger pinch opens it, with Fondo · Widgets · Estilo de iconos · Ajustes; the choices are kept. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "es-w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EditHomeTest {
    @get:Rule val compose = createComposeRule()
    private val values = MapValues()
    private val state by lazy { HomeState(values) { LocalDateTime.of(2026, 11, 25, 12, 22) } }
    private var wallpaperAsked = 0
    private fun app(label: String, pkg: String, module: String? = null) = AppEntry(pkg, "$pkg.Main", label, module)
    private val apps = listOf(app("Reloj", "com.zirakusa.reloj", "clock_"), app("Calendario", "com.zirakusa.calendario", "calendar_"),
        app("Notas", "com.zirakusa.notas", "notes_"), app("Teléfono", "com.android.dialer"), app("Chrome", "com.android.chrome"))

    @Test fun pinchRules() {
        assertTrue(Pinch.closed(400f, 280f)); assertFalse(Pinch.closed(400f, 300f))          // must come together enough
        assertFalse(Pinch.closed(80f, 10f))                                                     // fingers that start too close
        assertEquals(500f, Pinch.distance(0f, 0f, 300f, 400f))
        assertEquals(IconShape.CIRCLE, IconShape.from("circle")); assertEquals(IconShape.SYSTEM, IconShape.from(null))
        val s = HomeState(MapValues()); s.page = Page.SETTINGS; s.startEditing(); assertFalse(s.editing)   // only from the home screen
    }

    @Test fun pinchOpensEditHome() {
        state.load(apps) { listOf("com.android.dialer/com.android.dialer.Main", "com.android.chrome/com.android.chrome.Main") }
        state.changeWallpaper(false); state.finishWelcome()
        compose.setContent { HomeTheme(state.themeMode, state.accent) { LauncherApp(state, AppActions(wallpaper = { wallpaperAsked++ }), ticking = false) } }
        compose.onRoot().performTouchInput { pinch(Offset(100f, 500f), Offset(60f, 900f), Offset(700f, 500f), Offset(380f, 900f)) }
        compose.waitForIdle()
        assertTrue("a pinch opens «Editar inicio»", state.editing)
        listOf("Fondo de pantalla", "Widgets", "Estilo de iconos", "Ajustes").forEach { compose.onNodeWithText(it).assertExists() }
        compose.waitForIdle(); onView(isRoot()).captureRoboImage("build/screenshots/inicio-edit.png")

        compose.onNodeWithText("Fondo de pantalla").performClick(); assertEquals(1, wallpaperAsked)
        compose.onNodeWithText("Estilo de iconos").performClick(); assertEquals(EditSheet.ICONS, state.editSheet)
        compose.onNodeWithText("Círculo").performClick(); assertEquals(IconShape.CIRCLE, state.iconShape)
        assertEquals(IconShape.CIRCLE, HomeState(values).iconShape)                              // kept
        compose.waitForIdle(); onView(isRoot()).captureRoboImage("build/screenshots/inicio-edit-icons.png")
        compose.onNodeWithText("Hecho").performClick(); assertEquals(null, state.editSheet)

        compose.onNodeWithText("Widgets").performClick()
        compose.onNodeWithText("Lista").performClick(); assertEquals(HomeStyle.LIST, state.style)
        compose.onNodeWithText("Hecho").performClick()
        state.stopEditing(); assertFalse(state.editing)
    }
}
