package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.ListStyle
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

/**
 * «Lista»: the clock, date and today's first event, the favourites as a list (dock first, then suggestions), and the
 * alphabet on the right: slide a finger over it (the letters part like a wave) and let go to open the apps at that letter.
 */
@Composable fun ListHome(state: HomeState, actions: AppActions) {
    val ink = if (state.wallpaper) Color.White else HomeColors.Text
    if (state.calendarPreview) LaunchedEffect(state.apps) { state.cards = withContext(Dispatchers.IO) { actions.cards(state.apps) } }
    Box(Modifier.fillMaxSize().background(if (state.wallpaper) Color(0x55000000) else HomeColors.Background)
        .pinchIn(state::startEditing)
        .combinedClickableLong { state.section = Section.MENU; state.page = Page.SETTINGS }
        .pointerInput(Unit) { var total = 0f; detectHorizontalDragGestures(onDragStart = { total = 0f },
            onDragEnd = { if (total > 60.dp.toPx()) state.page = Page.FEED }) { _, dx -> total += dx } }
        .statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize().padding(start = 28.dp, end = 72.dp)) {
            Spacer(Modifier.weight(0.6f))
            val t = state.now
            Text(if (state.h24) "%02d:%02d".format(t.hour, t.minute) else "%d:%02d".format((t.hour % 12).let { if (it == 0) 12 else it }, t.minute),
                Modifier.combinedClickableCompat { actions.clock() }, fontSize = 64.sp, fontWeight = FontWeight.Light, color = ink)
            val l = locale()
            Text(DateTimeFormatter.ofPattern("EEE, d MMM", l).format(t).replace(".", ""), Modifier.combinedClickableCompat { actions.calendar() },
                fontSize = 18.sp, color = ink)
            state.calendarLine?.let { line ->
                Row(Modifier.padding(top = 6.dp).combinedClickableCompat { actions.calendar() }, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Event, null, Modifier.size(16.dp), tint = ink.copy(alpha = 0.8f))
                    Text(line, Modifier.padding(start = 6.dp), fontSize = 15.sp, color = ink.copy(alpha = 0.8f), maxLines = 1)
                }
            }
            Spacer(Modifier.size(24.dp))
            if (state.favourites.isEmpty()) Text(stringResource(R.string.dock_empty), color = ink.copy(alpha = 0.7f), fontSize = 15.sp)
            state.favourites.forEach { (app, suggested) -> Favourite(app, suggested, state, actions, ink) }
            Spacer(Modifier.weight(1f))
        }
        AlphabetBar(ListStyle.letters(state.drawer), ink, Modifier.align(Alignment.CenterEnd)) { state.openAt(it) }
        IconButton({ state.openDrawer(search = true) }, Modifier.align(Alignment.BottomEnd).padding(16.dp).size(56.dp).clip(RoundedCornerShape(28.dp))
            .background(HomeColors.Card.copy(alpha = 0.85f))) { Icon(Icons.Outlined.Search, stringResource(R.string.search_apps), tint = HomeColors.Text) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable private fun Favourite(app: AppEntry, suggested: Boolean, state: HomeState, actions: AppActions, ink: Color) =
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).combinedClickable(onClickLabel = app.label, onClick = { actions.open(app) },
        onLongClick = { state.menuFor = app }).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AppImage(app, actions, 44.dp)
        Text(app.label, Modifier.padding(start = 18.dp), fontSize = 19.sp, color = if (suggested) ink.copy(alpha = 0.6f) else ink, maxLines = 1)
    }

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableCompat(onClick: () -> Unit) = combinedClickable(onClick = onClick)

/** Holding the background opens «Ajustes de Inicio». */
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableLong(onLong: () -> Unit) = combinedClickable(onClick = {}, onLongClick = onLong)

/** The alphabet on the right edge: touch and slide; the letters near the finger move left in a wave; letting go picks one. */
@Composable fun AlphabetBar(letters: List<Char>, ink: Color, modifier: Modifier, onPick: (Char) -> Unit) {
    if (letters.isEmpty()) return
    var height by remember { mutableFloatStateOf(0f) }
    var touch by remember { mutableStateOf<Float?>(null) }
    val max = with(LocalDensity.current) { 56.dp.toPx() }
    Column(modifier.fillMaxHeight(0.62f).width(48.dp).onSizeChanged { height = it.height.toFloat() }
        .semantics { contentDescription = "A-Z" }
        .pointerInput(letters) { detectTapGestures { onPick(letters[ListStyle.letterAt(it.y, height, letters.size)]) } }
        .pointerInput(letters) {
            detectVerticalDragGestures(
                onDragStart = { touch = ListStyle.letterAt(it.y, height, letters.size).toFloat() },
                onDragEnd = { touch?.let { onPick(letters[it.toInt()]) }; touch = null },
                onDragCancel = { touch = null },
            ) { change, _ -> touch = ListStyle.letterAt(change.position.y, height, letters.size).toFloat() }
        }, verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
        letters.forEachIndexed { i, c ->
            val shift = ListStyle.wave(i, touch, max)
            val on = touch?.toInt() == i
            Text(c.toString(), Modifier.offset(x = with(LocalDensity.current) { -shift.toDp() }), fontSize = if (on) 22.sp else 14.sp,
                fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, color = if (on) HomeColors.AccentText else ink)
        }
    }
}
