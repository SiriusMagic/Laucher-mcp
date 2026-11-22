package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.background
import com.zirakusa.launcher.data.HomeStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import com.zirakusa.launcher.data.Apps
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeColors

/** All apps: the search field on top, then «Familia» (apps that speak MCP) and every app A to Z; searching shows the matches. */
@Composable fun DrawerScreen(state: HomeState, actions: AppActions) = androidx.compose.foundation.layout.Column(
    Modifier.fillMaxSize().background(HomeColors.Background).statusBarsPadding().navigationBarsPadding().imePadding()) {
    val focus = remember { FocusRequester() }
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        TextField(state.query, { state.query = it.take(40) }, Modifier.weight(1f).clip(CircleShape).focusRequester(focus), singleLine = true,
            placeholder = { Text(stringResource(R.string.search_apps)) }, leadingIcon = { Icon(Icons.Outlined.Search, null) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { state.results.firstOrNull()?.let(actions.open) }),
            colors = TextFieldDefaults.colors(focusedContainerColor = HomeColors.Card, unfocusedContainerColor = HomeColors.Card,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = HomeColors.Accent))
        IconButton({ state.page = Page.SETTINGS }) { Icon(Icons.Outlined.Settings, stringResource(R.string.settings), tint = HomeColors.Text) }
    }
    LaunchedEffect(state.drawerOpened) { if (state.searching) runCatching { focus.requestFocus() } }
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val recent = if (state.showRecent) state.recentApps.take(state.columns) else emptyList()
    val family = if (state.familyFirst) state.family else emptyList()
    // Where «Todas» starts: captions and rows before it, so the letters on the right can jump there.
    val before = (if (recent.isEmpty()) 0 else 2) + (if (family.isEmpty()) 0 else 1 + (family.size + state.columns - 1) / state.columns) + 1
    val listStyle = state.style == HomeStyle.LIST
    val all = state.drawerOrdered
    val groups = Apps.groups(all).map { (c, _) -> c to all.filter { Apps.letter(it) == c } }
    // Where each letter starts in the list, for the letters on the right and the alphabet of the home screen.
    val positions = buildMap {
        var at = before
        if (listStyle) groups.forEach { (c, apps) -> put(c, at); at += 1 + apps.size }
        else all.withIndex().groupBy { Apps.letter(it.value) }.forEach { (c, items) -> put(c, before + items.first().index / state.columns) }
    }
    LaunchedEffect(state.drawerOpened, state.jumpTo) { list.scrollToItem(state.jumpTo?.let { positions[it] } ?: 0) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(end = if (state.query.isBlank()) 20.dp else 0.dp), list) {
            if (state.query.isNotBlank()) {
                if (state.results.isEmpty()) item { Text(stringResource(R.string.no_apps), Modifier.fillMaxWidth().padding(32.dp), color = HomeColors.Muted, fontSize = 16.sp) }
                grid(state.results, state, actions)
            } else {
                if (recent.isNotEmpty()) { item { Caption(stringResource(R.string.recent)) }; grid(recent, state, actions) }
                if (family.isNotEmpty()) { item { Caption(stringResource(R.string.family)) }; grid(family, state, actions) }
                item { Caption(stringResource(R.string.all_apps)) }
                if (listStyle) groups.forEach { (c, apps) ->
                    val dim = state.jumpTo != null && state.jumpTo != c
                    item(key = "h$c") { Text(c.toString(), Modifier.padding(start = 24.dp, top = 12.dp), fontSize = 26.sp, fontWeight = FontWeight.Medium,
                        color = if (dim) HomeColors.Muted.copy(alpha = 0.4f) else HomeColors.Text) }
                    apps.forEach { app -> item(key = "r${app.key}") { AppRow(app, state, actions, dim) } }
                } else grid(all, state, actions, "all")
            }
            item { Spacer(Modifier.padding(bottom = 24.dp)) }
        }
        if (state.query.isBlank()) Column(Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(vertical = 16.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
            positions.forEach { (letter, at) ->
                Text(letter.toString(), Modifier.clickable { state.jumpTo = letter; scope.launch { list.scrollToItem(at) } },
                    fontSize = 12.sp, color = HomeColors.AccentText)
            }
        }
    }
}

private fun LazyListScope.grid(apps: List<AppEntry>, state: HomeState, actions: AppActions, prefix: String = "") =
    apps.chunked(state.columns).forEachIndexed { i, row ->
        item(key = if (prefix.isEmpty()) null else "$prefix$i") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                row.forEach { AppCell(it, actions, state, Modifier.weight(1f), size = if (state.columns == 5) 48.dp else 54.dp) }
                repeat(state.columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }

/** «Lista»: one app per row, icon and name; dimmed when another letter was picked on the alphabet. */
@OptIn(ExperimentalFoundationApi::class)
@Composable private fun AppRow(app: AppEntry, state: HomeState, actions: AppActions, dim: Boolean) =
    Row(Modifier.fillMaxWidth().alpha(if (dim) 0.35f else 1f).combinedClickable(onClickLabel = app.label, onClick = { actions.open(app) },
        onLongClick = { state.menuFor = app }).padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AppImage(app, actions, 44.dp)
        Text(app.label, Modifier.padding(start = 18.dp), fontSize = 18.sp, maxLines = 1)
    }
