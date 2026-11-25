package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.Pinch
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.EditSheet
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.Page

/**
 * Two fingers coming together on the home screen ([Pinch]) call [onPinch]. It only watches: taps, swipes and holds with
 * one finger reach the home screen as before.
 */
fun Modifier.pinchIn(onPinch: () -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var start = 0f
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val down = event.changes.filter { it.pressed }
            if (down.isEmpty()) break
            if (down.size < 2) { start = 0f; continue }
            val d = Pinch.distance(down[0].position.x, down[0].position.y, down[1].position.x, down[1].position.y)
            if (start == 0f) start = d
            else if (Pinch.closed(start, d)) { down.forEach { it.consume() }; onPinch(); break }
        }
    }
}

/**
 * «Editar inicio», like a phone's own launcher after a pinch: the home small in a card (a tap or «Atrás» goes back), and
 * Fondo de pantalla · Widgets · Estilo de iconos · Ajustes under it.
 */
@Composable fun EditHome(state: HomeState, actions: AppActions) {
    Box(Modifier.fillMaxSize().background(Color(0xFF0E1116))) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(top = 24.dp, bottom = 12.dp), contentAlignment = Alignment.Center) {
                // The real home, small and still: a tap anywhere on it leaves «Editar inicio».
                Box(Modifier.fillMaxSize().graphicsLayer { scaleX = 0.8f; scaleY = 0.8f }.clip(RoundedCornerShape(36.dp))) {
                    HomeScreen(state, actions)
                    Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, null, onClickLabel = stringResource(R.string.done),
                        onClick = state::stopEditing))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                EditButton(Icons.Outlined.Wallpaper, R.string.edit_wallpaper) { actions.wallpaper() }
                EditButton(Icons.Outlined.Widgets, R.string.edit_widgets) { state.editSheet = EditSheet.WIDGETS }
                EditButton(Icons.Outlined.Apps, R.string.edit_icons) { state.editSheet = EditSheet.ICONS }
                EditButton(Icons.Outlined.Settings, R.string.settings) {
                    state.stopEditing(); state.section = Section.MENU; state.page = Page.SETTINGS
                }
            }
        }
        when (state.editSheet) {
            EditSheet.WIDGETS -> WidgetsSheet(state) { state.editSheet = null }
            EditSheet.ICONS -> IconsSheet(state, actions) { state.editSheet = null }
            null -> Unit
        }
    }
}

@Composable private fun EditButton(icon: ImageVector, label: Int, onClick: () -> Unit) =
    Column(Modifier.width(84.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF2C313A)), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(26.dp), tint = Color.White)
        }
        Text(stringResource(label), Modifier.padding(top = 8.dp), color = Color.White, fontSize = 13.sp, maxLines = 1,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
