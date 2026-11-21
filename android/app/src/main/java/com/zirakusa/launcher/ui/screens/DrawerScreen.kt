package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.background
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
    LaunchedEffect(state.drawerOpened) { list.scrollToItem(0) }
    LazyColumn(Modifier.fillMaxSize(), list) {
        if (state.query.isNotBlank()) {
            if (state.results.isEmpty()) item { Text(stringResource(R.string.no_apps), Modifier.fillMaxWidth().padding(32.dp), color = HomeColors.Muted, fontSize = 16.sp) }
            grid(state.results, state, actions)
        } else {
            if (state.familyFirst && state.family.isNotEmpty()) {
                item { Caption(stringResource(R.string.family)) }
                grid(state.family, state, actions)
            }
            item { Caption(stringResource(R.string.all_apps)) }
            grid(state.drawer, state, actions, "all")
        }
        item { Spacer(Modifier.padding(bottom = 24.dp)) }
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
