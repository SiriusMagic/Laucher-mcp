package com.zirakusa.launcher.ui.screens

import com.zirakusa.launcher.IconCard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.zirakusa.launcher.data.HomeStyle
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

/** A category of «Ajustes de Inicio». */
enum class Section { MENU, HOME, DRAWER, THEMES, ABOUT }

/** «Ajustes de Inicio»: a list of categories with their icons (like the phone's own settings); each opens its page. */
@Composable fun SettingsScreen(state: HomeState, actions: AppActions) {
    var privacy by remember { mutableStateOf(false) }
    val section = state.section
    Column(Modifier.fillMaxSize().background(HomeColors.Background).systemBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PageTop(stringResource(when (section) { Section.MENU -> R.string.settings; Section.HOME -> R.string.home_screen; Section.DRAWER -> R.string.drawer
            Section.THEMES -> R.string.themes; Section.ABOUT -> R.string.about })) { if (section == Section.MENU) state.page = Page.HOME else state.section = Section.MENU }
        when (section) {
            Section.MENU -> {
                if (!state.isDefault) Group { Item(stringResource(R.string.default_home), stringResource(R.string.default_no), onClick = actions.makeDefault) }
                Category(Icons.Outlined.Settings, Color(0xFF4F8DF7), R.string.cat_system, R.string.cat_system_hint, onClick = actions.systemSettings)
                Category(Icons.Outlined.Wallpaper, Color(0xFFE8685A), R.string.wallpaper_change, R.string.cat_wallpaper_hint, onClick = actions.wallpaper)
                Category(Icons.Outlined.Palette, Color(0xFFF2B33D), R.string.themes, R.string.cat_themes_hint) { state.section = Section.THEMES }
                Category(Icons.Outlined.PhoneAndroid, Color(0xFFB57BEE), R.string.home_screen, R.string.cat_home_hint) { state.section = Section.HOME }
                Category(Icons.Outlined.Apps, Color(0xFFF59E42), R.string.drawer, R.string.cat_drawer_hint) { state.section = Section.DRAWER }
                Category(Icons.Outlined.Dashboard, Color(0xFFEB4D3D), R.string.cat_today, R.string.cat_today_hint) { state.editCards(Page.SETTINGS) }
                val status = when {
                    !McpMode.active -> stringResource(R.string.mcp_status_off)
                    McpMode.hubRedirect -> stringResource(R.string.mcp_redirect)
                    McpMode.state == RelayLink.State.ONLINE -> stringResource(R.string.mcp_online)
                    McpMode.state == RelayLink.State.CONNECTING -> stringResource(R.string.mcp_connecting)
                    else -> stringResource(R.string.mcp_offline)
                }
                Category(Icons.Outlined.Hub, Color(0xFF2BB673), R.string.mcp_title, null, "$status · ${stringResource(R.string.mcp_settings_hint)}") { state.page = Page.MCP }
                Category(Icons.Outlined.Info, Color(0xFF8C93A3), R.string.about, R.string.cat_about_hint) { state.section = Section.ABOUT }
            }
            Section.HOME -> Group {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.style), fontSize = 18.sp)
                    Segmented(listOf(stringResource(R.string.style_classic), stringResource(R.string.style_list)), state.style.ordinal, Modifier.fillMaxWidth()) {
                        state.changeStyle(HomeStyle.entries[it])
                    }
                    Text(stringResource(if (state.style == HomeStyle.LIST) R.string.style_list_hint else R.string.style_classic_hint), color = HomeColors.Muted, fontSize = 14.sp)
                }
                Divider()
                SwitchItem(stringResource(R.string.calendar_preview), stringResource(R.string.calendar_preview_hint), state.calendarPreview, onChange = state::changeCalendarPreview)
                Divider()
                SwitchItem(stringResource(R.string.suggest), stringResource(R.string.suggest_hint), state.suggest, onChange = state::changeSuggest)
                Divider()
                Item(stringResource(R.string.default_home), stringResource(if (state.isDefault) R.string.default_yes else R.string.default_no), onClick = actions.makeDefault)
                Divider()
                SwitchItem(stringResource(R.string.show_clock), stringResource(R.string.show_clock_hint), state.showClock, onChange = state::changeClock)
                Divider()
                SwitchItem(stringResource(R.string.use_wallpaper), null, state.wallpaper, onChange = state::changeWallpaper)
                Divider()
                Item(stringResource(R.string.welcome_again), null) { state.step = 0; state.picked = state.dock; state.page = Page.WELCOME }
            }
            Section.DRAWER -> Group {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.columns), fontSize = 18.sp)
                    Segmented(Settings.COLUMNS.map { it.toString() }, Settings.COLUMNS.indexOf(state.columns), Modifier.fillMaxWidth()) { state.changeColumns(Settings.COLUMNS[it]) }
                }
                Divider()
                SwitchItem(stringResource(R.string.family_first), stringResource(R.string.family_first_hint), state.familyFirst, onChange = state::changeFamilyFirst)
                Divider()
                SwitchItem(stringResource(R.string.show_recent), null, state.showRecent, onChange = state::changeShowRecent)
                Divider()
                SwitchItem(stringResource(R.string.sort_usage), stringResource(R.string.sort_usage_hint), state.sortByUsage, onChange = state::changeSortByUsage)
                Divider()
                Item(stringResource(R.string.hidden_apps), state.hidden.size.toString()) { state.page = Page.HIDDEN }
            }
            Section.THEMES -> {
                ThemeCard(state.themeMode, state.accent, state::changeTheme, state::changeAccent)
                IconCard(HomeColors.Card, HomeColors.Text, HomeColors.Muted, HomeColors.Accent, androidx.compose.ui.graphics.Color(0xFF000000))
            }
            Section.ABOUT -> Group {
                Item(stringResource(R.string.privacy), null) { privacy = true }
                Divider()
                Item(stringResource(R.string.version_title), BuildConfig.VERSION_NAME, onClick = null)
            }
        }
        Text("", Modifier.padding(bottom = 16.dp))
    }
    if (privacy) AlertDialog({ privacy = false }, confirmButton = { TextButton({ privacy = false }) { Text(stringResource(R.string.done), color = HomeColors.AccentText) } },
        title = { Text(stringResource(R.string.privacy)) }, text = { Text(stringResource(R.string.privacy_text)) })
}

/** One category: a coloured round icon, its name and what is inside. */
@Composable private fun Category(icon: ImageVector, tint: Color, title: Int, hint: Int?, hintText: String? = null, onClick: () -> Unit) =
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(tint.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint) }
        Column(Modifier.padding(start = 18.dp)) {
            Text(stringResource(title), fontSize = 18.sp)
            Text(hintText ?: stringResource(hint!!), color = HomeColors.Muted, fontSize = 14.sp)
        }
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
