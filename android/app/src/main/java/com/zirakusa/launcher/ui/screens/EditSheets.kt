package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.HomeStyle
import com.zirakusa.launcher.data.IconShape
import com.zirakusa.launcher.data.Settings
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeColors

/** A panel from the bottom over «Editar inicio», so the small home above shows each change at once. */
@Composable private fun Panel(title: Int, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) =
    Box(Modifier.fillMaxSize().background(Color(0x66000000)).clickable(onClick = onClose), contentAlignment = Alignment.BottomCenter) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(HomeColors.Sheet)
            .clickable(enabled = false) {}.navigationBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 12.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(title), Modifier.weight(1f), fontSize = 20.sp)
                TextButton(onClose) { Text(stringResource(R.string.done), color = HomeColors.AccentText) }
            }
            content()
        }
    }

/** «Widgets»: the home's design and what it shows (clock, calendar, suggestions), and the «Hoy» cards. */
@Composable fun WidgetsSheet(state: HomeState, onClose: () -> Unit) = Panel(R.string.edit_widgets, onClose) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.style), fontSize = 17.sp)
        Segmented(listOf(stringResource(R.string.style_widgets), stringResource(R.string.style_classic), stringResource(R.string.style_list)), state.style.ordinal,
            Modifier.fillMaxWidth()) { state.changeStyle(HomeStyle.entries[it]) }
        Text(stringResource(when (state.style) { HomeStyle.WIDGETS -> R.string.style_widgets_hint; HomeStyle.CLASSIC -> R.string.style_classic_hint
            HomeStyle.LIST -> R.string.style_list_hint }), color = HomeColors.Muted, fontSize = 14.sp)
    }
    SwitchItem(stringResource(R.string.show_clock), stringResource(R.string.show_clock_hint), state.showClock, onChange = state::changeClock)
    SwitchItem(stringResource(R.string.calendar_preview), stringResource(R.string.calendar_preview_hint), state.calendarPreview, onChange = state::changeCalendarPreview)
    SwitchItem(stringResource(R.string.suggest), stringResource(R.string.suggest_hint), state.suggest, onChange = state::changeSuggest)
    Item(stringResource(R.string.edit_view), stringResource(R.string.edit_view_hint)) { state.stopEditing(); state.editCards(Page.HOME) }
}

/** «Estilo de iconos»: the shape of every icon (shown on four of your apps) and how many go in a row. */
@Composable fun IconsSheet(state: HomeState, actions: AppActions, onClose: () -> Unit) = Panel(R.string.edit_icons, onClose) {
    val sample = (state.dockApps + state.family).distinctBy { it.key }.take(4)
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        IconShape.entries.forEach { shape ->
            val on = shape == state.iconShape
            Column(Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClickLabel = stringResource(shapeName(shape))) { state.changeIconShape(shape) }
                .border(if (on) 2.dp else 0.dp, if (on) HomeColors.Accent else Color.Transparent, RoundedCornerShape(16.dp)).padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                CompositionLocalProvider(LocalIconShape provides shape) {
                    val app = sample.firstOrNull()
                    if (app != null) AppImage(app, actions, 44.dp)
                    else Box(Modifier.size(44.dp).clip(RoundedCornerShape(44.dp * (if (shape.corner >= 0) shape.corner else 0.3f))).background(HomeColors.Accent))
                }
                Text(stringResource(shapeName(shape)), Modifier.padding(top = 6.dp), color = if (on) HomeColors.AccentText else HomeColors.Text, fontSize = 13.sp)
            }
        }
    }
    Text(stringResource(R.string.icon_shape_hint), Modifier.padding(horizontal = 20.dp), color = HomeColors.Muted, fontSize = 13.sp)
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.columns), fontSize = 17.sp)
        Segmented(Settings.COLUMNS.map { it.toString() }, Settings.COLUMNS.indexOf(state.columns).coerceAtLeast(0), Modifier.fillMaxWidth()) {
            state.changeColumns(Settings.COLUMNS[it])
        }
    }
}

fun shapeName(s: IconShape) = when (s) {
    IconShape.SYSTEM -> R.string.shape_system; IconShape.CIRCLE -> R.string.shape_circle
    IconShape.ROUNDED -> R.string.shape_rounded; IconShape.SQUARE -> R.string.shape_square
}
