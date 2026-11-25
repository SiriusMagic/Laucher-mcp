package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.Apps
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.theme.HomeColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** «Estilo de iconos» for every icon below it (the launcher provides the chosen one). */
val LocalIconShape = androidx.compose.runtime.staticCompositionLocalOf { com.zirakusa.launcher.data.IconShape.SYSTEM }

/** The app's icon (loaded in the background), or its first letter on a coloured circle while there is none. */
@Composable fun AppImage(app: AppEntry, actions: AppActions, size: Dp) {
    val px = with(LocalDensity.current) { size.roundToPx() }
    val shape = LocalIconShape.current
    val icon by produceState<ImageBitmap?>(null, app.key, shape) { value = withContext(Dispatchers.IO) { actions.icon(app, px, shape) } }
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        val bitmap = icon
        if (bitmap != null) Image(bitmap, null, Modifier.size(size))
        else Box(Modifier.size(size).clip(RoundedCornerShape(size * (if (shape.corner >= 0) shape.corner else 0.3f))).background(placeholder(app)), contentAlignment = Alignment.Center) {
            Text(Apps.letter(app).toString(), color = Color.White, fontSize = (size.value * 0.42f).sp, fontWeight = FontWeight.Medium)
        }
    }
}

private val shades = listOf(0xFF5B7CFA, 0xFF2BB673, 0xFFF28B30, 0xFFE0457B, 0xFF8E6CEF, 0xFF1FA2C4, 0xFFD4A017).map { Color(it) }
private fun placeholder(app: AppEntry) = shades[Math.floorMod(app.pkg.hashCode(), shades.size)]

/** One app in a grid or the dock: icon and (unless [label] is false) its name; tap opens, hold opens its menu. */
@OptIn(ExperimentalFoundationApi::class)
@Composable fun AppCell(app: AppEntry, actions: AppActions, state: HomeState, modifier: Modifier = Modifier, size: Dp = 54.dp, label: Boolean = true,
    labelColor: Color = HomeColors.Text) =
    Column(modifier.clip(RoundedCornerShape(16.dp)).combinedClickable(onClickLabel = app.label, onClick = { actions.open(app) },
        onLongClick = { state.menuFor = app }).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        AppImage(app, actions, size)
        if (label) Text(app.label, Modifier.fillMaxWidth().padding(top = 6.dp, start = 2.dp, end = 2.dp), fontSize = 12.sp, maxLines = 1,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, color = labelColor)
    }

/** The long-press menu: add to or remove from the dock, hide, app info and uninstall. */
@Composable fun AppMenu(state: HomeState, actions: AppActions) {
    val app = state.menuFor ?: return
    val close = { state.menuFor = null }
    Dialog(close) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(HomeColors.Card).padding(vertical = 16.dp)) {
            Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AppImage(app, actions, 56.dp)
                Text(app.label, Modifier.padding(top = 8.dp), fontSize = 19.sp)
                if (app.family) Text(stringResource(R.string.family_mcp, app.module.orEmpty()), color = HomeColors.AccentText, fontSize = 13.sp)
            }
            if (app.key in state.dock) Entry(Icons.Outlined.RemoveCircleOutline, stringResource(R.string.dock_remove)) { state.removeFromDock(app); close() }
            else Entry(Icons.AutoMirrored.Outlined.PlaylistAdd, stringResource(R.string.dock_add)) {
                if (!state.addToDock(app)) actions.toast(R.string.dock_full); close()
            }
            Entry(Icons.Outlined.VisibilityOff, stringResource(R.string.hide)) { state.hide(app); close() }
            Entry(Icons.Outlined.Info, stringResource(R.string.app_info)) { actions.info(app); close() }
            Entry(Icons.Outlined.DeleteOutline, stringResource(R.string.uninstall), HomeColors.Danger) { actions.uninstall(app); close() }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable private fun Entry(icon: ImageVector, text: String, color: Color = HomeColors.Text, onClick: () -> Unit) =
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color)
        Text(text, Modifier.padding(start = 16.dp), fontSize = 17.sp, color = color)
    }
