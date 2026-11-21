package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.BuildConfig
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.Settings
import com.zirakusa.launcher.mcp.McpMode
import com.zirakusa.launcher.mcp.RelayLink
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeColors
import java.util.Locale

/** The phone's language. */
@Composable fun locale(): Locale = LocalConfiguration.current.locales[0]

@Composable fun PageTop(title: String, onBack: () -> Unit) = Row(Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
    RoundButton(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back), size = 52.dp, onClick = onBack)
    Text(title, Modifier.padding(start = 16.dp), fontSize = 22.sp)
}

@Composable fun Divider() = androidx.compose.material3.HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = HomeColors.Chip)

/** «Ajustes»: default home app, home screen, drawer, hidden apps, Modo MCP, Temas, privacy and version. */
@Composable fun SettingsScreen(state: HomeState, actions: AppActions) {
    var privacy by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(HomeColors.Background).systemBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PageTop(stringResource(R.string.settings)) { state.page = Page.HOME }
        Group {
            Item(stringResource(R.string.default_home), stringResource(if (state.isDefault) R.string.default_yes else R.string.default_no), onClick = actions.makeDefault)
            Divider()
            Item(stringResource(R.string.welcome_again), null) { state.step = 0; state.picked = state.dock; state.page = Page.WELCOME }
        }
        Caption(stringResource(R.string.home_screen))
        Group {
            SwitchItem(stringResource(R.string.show_clock), stringResource(R.string.show_clock_hint), state.showClock, onChange = state::changeClock)
            Divider()
            SwitchItem(stringResource(R.string.use_wallpaper), null, state.wallpaper, onChange = state::changeWallpaper)
            Divider()
            Item(stringResource(R.string.wallpaper_change), null, onClick = actions.wallpaper)
        }
        Caption(stringResource(R.string.drawer))
        Group {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.columns), fontSize = 18.sp)
                Segmented(Settings.COLUMNS.map { it.toString() }, Settings.COLUMNS.indexOf(state.columns), Modifier.fillMaxWidth()) { state.changeColumns(Settings.COLUMNS[it]) }
            }
            Divider()
            SwitchItem(stringResource(R.string.family_first), stringResource(R.string.family_first_hint), state.familyFirst, onChange = state::changeFamilyFirst)
            Divider()
            SwitchItem(stringResource(R.string.show_recent), null, state.showRecent, onChange = state::changeShowRecent)
            Divider()
            Item(stringResource(R.string.hidden_apps), state.hidden.size.toString()) { state.page = Page.HIDDEN }
        }
        Group {
            val status = when {
                !McpMode.active -> stringResource(R.string.mcp_status_off)
                McpMode.hubRedirect -> stringResource(R.string.mcp_redirect)
                McpMode.state == RelayLink.State.ONLINE -> stringResource(R.string.mcp_online)
                McpMode.state == RelayLink.State.CONNECTING -> stringResource(R.string.mcp_connecting)
                else -> stringResource(R.string.mcp_offline)
            }
            Item(stringResource(R.string.mcp_title), "$status · ${stringResource(R.string.mcp_settings_hint)}") { state.page = Page.MCP }
        }
        ThemeCard(state.themeMode, state.accent, state::changeTheme, state::changeAccent)
        Caption(stringResource(R.string.about))
        Group {
            Item(stringResource(R.string.privacy), null) { privacy = true }
            Divider()
            Item(stringResource(R.string.version_title), BuildConfig.VERSION_NAME, onClick = null)
        }
        Text("", Modifier.padding(bottom = 16.dp))
    }
    if (privacy) AlertDialog({ privacy = false }, confirmButton = { TextButton({ privacy = false }) { Text(stringResource(R.string.done), color = HomeColors.AccentText) } },
        title = { Text(stringResource(R.string.privacy)) }, text = { Text(stringResource(R.string.privacy_text)) })
}

/** «Apps ocultas»: each with «Mostrar». */
@Composable fun HiddenScreen(state: HomeState, actions: com.zirakusa.launcher.ui.AppActions) =
    Column(Modifier.fillMaxSize().background(HomeColors.Background).systemBarsPadding().padding(horizontal = 16.dp)) {
        PageTop(stringResource(R.string.hidden_apps)) { state.page = Page.SETTINGS }
        if (state.hiddenApps.isEmpty()) Text(stringResource(R.string.no_hidden), Modifier.padding(16.dp), color = HomeColors.Muted, fontSize = 16.sp)
        LazyColumn { items(state.hiddenApps, key = { it.key }) { app ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                AppImage(app, actions, 44.dp)
                Text(app.label, Modifier.weight(1f).padding(start = 16.dp), fontSize = 17.sp)
                TextButton({ state.unhide(app) }) { Text(stringResource(R.string.show), color = HomeColors.AccentText) }
            }
        } }
    }
