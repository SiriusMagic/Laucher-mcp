package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.Widgets
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.theme.HomeColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The «Widgets» home: analog clock (tap: Reloj) and four round buttons; the date card (tap: Calendario) and the next
 * alarm in a circle (tap: Reloj); the dock as round buttons and «Buscar» at the bottom. Gestures live in [HomeScreen].
 */
@Composable fun WidgetsHome(state: HomeState, actions: AppActions) =
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Spacer(Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AnalogClock(state, Modifier.weight(1f).clickable(onClickLabel = stringResource(R.string.open_clock)) { actions.clock() })
            val buttons = Widgets.buttons(state.family, state.frequent, state.dockApps)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                buttons.chunked(2).forEachIndexed { row, pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        pair.forEachIndexed { col, app -> RoundApp(app, state, actions, accent = row * 2 + col == Widgets.BUTTONS - 1) }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            DateCard(state, Modifier.weight(1f).clickable { actions.calendar() })
            AlarmDial(state, Modifier.weight(1f).clickable(onClickLabel = stringResource(R.string.open_clock)) { actions.clock() })
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            if (state.dockApps.isEmpty()) Text(stringResource(R.string.dock_empty), Modifier.padding(16.dp), color = HomeColors.Muted, fontSize = 14.sp)
            state.dockApps.forEach { RoundApp(it, state, actions) }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(CircleShape).background(HomeColors.Card).clickable { state.openDrawer(search = true) }
            .padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, null, tint = HomeColors.Muted)
            Text(stringResource(R.string.search), Modifier.padding(start = 12.dp).weight(1f), color = HomeColors.Muted, fontSize = 16.sp)
            Icon(Icons.Outlined.Apps, stringResource(R.string.all_apps), Modifier.clip(CircleShape).clickable { state.openDrawer() }.padding(4.dp), tint = HomeColors.Text)
        }
    }

/** An app as a dark circle with its icon (the accent one is in the theme's colour); tap opens, hold opens its menu. */
@OptIn(ExperimentalFoundationApi::class)
@Composable private fun RoundApp(app: AppEntry, state: HomeState, actions: AppActions, accent: Boolean = false) =
    Box(Modifier.size(60.dp).clip(CircleShape).background(if (accent) HomeColors.Accent else HomeColors.Card)
        .combinedClickable(onClickLabel = app.label, onClick = { actions.open(app) }, onLongClick = { state.menuFor = app }), contentAlignment = Alignment.Center) {
        Box(Modifier.size(34.dp).clip(CircleShape)) { AppImage(app, actions, 34.dp) }
    }

/** Black face, 60 ticks, white hands and the accent second hand. */
@Composable private fun AnalogClock(state: HomeState, modifier: Modifier) {
    val t = state.now
    val hands = Widgets.hands(t.hour, t.minute, t.second)
    val red = HomeColors.Accent
    val time = "%d:%02d".format(t.hour, t.minute)
    Canvas(modifier.aspectRatio(1f).clip(CircleShape).background(Color(0xFF0A0A0A)).semantics { contentDescription = time }) {
        val r = size.minDimension / 2
        repeat(60) { i ->
            rotate(i * 6f) {
                val long = i % 5 == 0
                drawLine(Color.White.copy(alpha = if (long) 1f else 0.55f), Offset(center.x, center.y - r * 0.92f),
                    Offset(center.x, center.y - r * (if (long) 0.78f else 0.85f)), strokeWidth = if (long) 2.dp.toPx() else 1.dp.toPx())
            }
        }
        hand(hands.hour, r * 0.5f, 3.5.dp.toPx(), Color.White)
        hand(hands.minute, r * 0.75f, 2.5.dp.toPx(), Color.White)
        hand(hands.second, r * 0.82f, 1.5.dp.toPx(), red, tail = r * 0.18f)
        drawCircle(red, 3.5.dp.toPx())
    }
}

private fun DrawScope.hand(angle: Float, length: Float, width: Float, color: Color, tail: Float = 0f) = rotate(angle) {
    drawLine(color, Offset(center.x, center.y + tail), Offset(center.x, center.y - length), strokeWidth = width, cap = StrokeCap.Round)
}

/** «SÁBADO · 21 · Noviembre» in a square card. */
@Composable private fun DateCard(state: HomeState, modifier: Modifier) {
    val l = locale()
    Column(modifier.aspectRatio(1f).clip(RoundedCornerShape(24.dp)).background(HomeColors.Card).padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Text(DateTimeFormatter.ofPattern("EEEE", l).format(state.now).uppercase(l), color = HomeColors.Muted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text(state.now.dayOfMonth.toString(), color = HomeColors.Text, fontSize = 56.sp, fontFamily = FontFamily.Serif)
        Text(DateTimeFormatter.ofPattern("LLLL", l).format(state.now).replaceFirstChar { it.titlecase(l) }, color = HomeColors.Text, fontSize = 20.sp, fontFamily = FontFamily.Serif)
    }
}

/** «ALARMA · 6:02 · LUN» (or «Sin alarmas») in a circle. */
@Composable private fun AlarmDial(state: HomeState, modifier: Modifier) {
    val l = locale()
    Column(modifier.aspectRatio(1f).clip(CircleShape).background(HomeColors.Card).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.alarm).uppercase(l), color = HomeColors.Muted, fontSize = 11.sp, letterSpacing = 2.sp)
        val at = state.nextAlarm
        if (at == null) { Text(stringResource(R.string.no_alarms), Modifier.padding(top = 8.dp), color = HomeColors.Text, fontSize = 16.sp); return@Column }
        val alarm = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val hour = if (state.h24) alarm.hour else (alarm.hour % 12).let { if (it == 0) 12 else it }
        Text("%d:%02d".format(hour, alarm.minute), color = HomeColors.Text, fontSize = 34.sp, fontFamily = FontFamily.Monospace)
        val day = if (alarm.toLocalDate() == state.now.toLocalDate()) stringResource(R.string.today_short)
            else DateTimeFormatter.ofPattern("EEE", l).format(alarm).replace(".", "")
        val half = if (state.h24) "" else " · " + stringResource(if (alarm.hour < 12) R.string.am else R.string.pm)
        Text((day + half).uppercase(l), color = HomeColors.Muted, fontSize = 11.sp, letterSpacing = 2.sp)
    }
}
