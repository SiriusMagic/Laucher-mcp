package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The home screen: the clock, date and next alarm on top (tap: Reloj / Calendario), the family apps, «Buscar apps» and
 * the dock. Swipe up or tap the apps button for the drawer, right for «Hoy»; hold the background for the wallpaper and Ajustes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable fun HomeScreen(state: HomeState, actions: AppActions) {
    if (state.style == com.zirakusa.launcher.data.HomeStyle.LIST) return ListHome(state, actions)
    var menu by remember { mutableStateOf(false) }
    // Over a wallpaper the text is white with a soft shade behind; on the theme's colour it follows the theme.
    val ink = if (state.wallpaper) Color.White else HomeColors.Text
    Box(Modifier.fillMaxSize().background(if (state.wallpaper) Brush.verticalGradient(listOf(Color(0x66000000), Color.Transparent, Color(0x88000000)))
        else Brush.verticalGradient(listOf(HomeColors.Background, HomeColors.Background)))
        .combinedClickable(onClick = {}, onLongClick = { menu = true })
        .pointerInput(Unit) { var total = 0f; detectVerticalDragGestures(onDragStart = { total = 0f },
            onDragEnd = { if (total < -60.dp.toPx()) state.openDrawer() }) { _, dy -> total += dy } }
        .pointerInput(Unit) { var total = 0f; detectHorizontalDragGestures(onDragStart = { total = 0f },
            onDragEnd = { if (total > 60.dp.toPx()) state.page = Page.FEED }) { _, dx -> total += dx } }) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            if (state.showClock) Clock(state, actions, ink)
            Spacer(Modifier.weight(1f))
            if (state.family.isNotEmpty()) {
                Text(stringResource(R.string.family), Modifier.padding(start = 24.dp, bottom = 4.dp), color = ink.copy(alpha = 0.8f), fontSize = 14.sp)
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    state.family.take(5).forEach { AppCell(it, actions, state, Modifier.weight(1f), labelColor = ink) }
                    repeat(5 - state.family.take(5).size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).clip(CircleShape).background(HomeColors.Card.copy(alpha = 0.85f))
                .clickable { state.openDrawer(search = true) }.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, null, tint = HomeColors.Muted)
                Text(stringResource(R.string.search_apps), Modifier.padding(start = 12.dp).weight(1f), color = HomeColors.Muted, fontSize = 16.sp)
                Icon(Icons.Outlined.Apps, stringResource(R.string.all_apps), Modifier.clip(CircleShape).clickable { state.openDrawer() }.padding(4.dp), tint = HomeColors.Text)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp).clip(RoundedCornerShape(32.dp))
                .background(HomeColors.Card.copy(alpha = 0.55f)).padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                if (state.dockApps.isEmpty()) Text(stringResource(R.string.dock_empty), Modifier.padding(16.dp), color = HomeColors.Muted, fontSize = 14.sp)
                state.dockApps.forEach { AppCell(it, actions, state, Modifier.weight(1f), size = 52.dp, label = false) }
            }
        }
        Box(Modifier.align(Alignment.Center)) {
            DropdownMenu(menu, { menu = false }) {
                DropdownMenuItem({ Text(stringResource(R.string.wallpaper_change)) }, { menu = false; actions.wallpaper() },
                    leadingIcon = { Icon(Icons.Outlined.Wallpaper, null) })
                DropdownMenuItem({ Text(stringResource(R.string.today_page)) }, { menu = false; state.page = Page.FEED })
                DropdownMenuItem({ Text(stringResource(R.string.edit_view)) }, { menu = false; state.editCards(Page.HOME) })
                DropdownMenuItem({ Text(stringResource(R.string.settings)) }, { menu = false; state.section = com.zirakusa.launcher.ui.screens.Section.MENU; state.page = Page.SETTINGS })
            }
        }
    }
}

/** «9:41», «sábado, 21 de noviembre» and «6:02 a. m.» (the next alarm). */
@Composable private fun Clock(state: HomeState, actions: AppActions, ink: Color) = Column(Modifier.padding(start = 28.dp, top = 40.dp, end = 28.dp)) {
    val l = locale()
    val t = state.now
    Text(if (state.h24) "%02d:%02d".format(t.hour, t.minute) else "%d:%02d".format((t.hour % 12).let { if (it == 0) 12 else it }, t.minute),
        Modifier.clickable { actions.clock() }, fontSize = 76.sp, fontWeight = FontWeight.Light, color = ink)
    Text(DateTimeFormatter.ofPattern(if (l.language == "es") "EEEE, d 'de' MMMM" else "EEEE, MMMM d", l).format(t).replaceFirstChar { it.titlecase() },
        Modifier.clickable { actions.calendar() }, fontSize = 19.sp, color = ink)
    state.nextAlarm?.let { at ->
        val alarm = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val time = if (state.h24) "%02d:%02d".format(alarm.hour, alarm.minute)
            else "%d:%02d %s".format((alarm.hour % 12).let { if (it == 0) 12 else it }, alarm.minute, stringResource(if (alarm.hour < 12) R.string.am else R.string.pm))
        val day = if (alarm.toLocalDate() == t.toLocalDate()) "" else DateTimeFormatter.ofPattern("EEE ", l).format(alarm).replace(".", "")
        Row(Modifier.padding(top = 6.dp).clickable { actions.clock() }, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Alarm, null, Modifier.size(18.dp), tint = ink.copy(alpha = 0.85f))
            Text(day + time, Modifier.padding(start = 6.dp), fontSize = 16.sp, color = ink.copy(alpha = 0.85f))
        }
    }
}
