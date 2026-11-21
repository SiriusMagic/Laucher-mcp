package com.zirakusa.launcher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ImageBitmap
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.ui.screens.AppMenu
import com.zirakusa.launcher.ui.screens.DrawerScreen
import com.zirakusa.launcher.ui.screens.HiddenScreen
import com.zirakusa.launcher.ui.screens.HomeScreen
import com.zirakusa.launcher.ui.screens.McpScreen
import com.zirakusa.launcher.ui.screens.SettingsScreen
import kotlinx.coroutines.delay

/** What the screens ask of the phone (the activity gives the real ones; tests leave the defaults). */
class AppActions(
    val icon: (AppEntry, Int) -> ImageBitmap? = { _, _ -> null },
    val open: (AppEntry) -> Unit = {},
    val info: (AppEntry) -> Unit = {},
    val uninstall: (AppEntry) -> Unit = {},
    val clock: () -> Unit = {},
    val calendar: () -> Unit = {},
    val wallpaper: () -> Unit = {},
    val makeDefault: () -> Unit = {},
    val toast: (Int) -> Unit = {},
    val copy: (String) -> Unit = {},
    val goToHub: () -> Unit = {},
)

/** The whole launcher: home, the drawer or a settings page; the app menu over any of them. [ticking] moves the clock. */
@Composable fun LauncherApp(state: HomeState, actions: AppActions = AppActions(), ticking: Boolean = true) {
    if (ticking) LaunchedEffect(Unit) { while (true) { state.tick(); delay(1000) } }
    when (state.page) {
        Page.HOME -> HomeScreen(state, actions)
        Page.DRAWER -> DrawerScreen(state, actions)
        Page.SETTINGS -> SettingsScreen(state, actions)
        Page.HIDDEN -> HiddenScreen(state, actions)
        Page.MCP -> McpScreen(actions.copy, actions.goToHub) { state.page = Page.SETTINGS }
    }
    AppMenu(state, actions)
}
