package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.Apps
import com.zirakusa.launcher.data.Dock
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.theme.HomeColors

/**
 * The first time: hello (no permissions asked), favourite apps for the dock, the wallpaper and the default home app.
 * «Omitir» on every step; «Continuar» / «¡Vamos!» at the bottom.
 */
@Composable fun WelcomeScreen(state: HomeState, actions: AppActions) =
    Column(Modifier.fillMaxSize().background(HomeColors.Background).systemBarsPadding().padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(state::finishWelcome) { Text(stringResource(R.string.skip), color = HomeColors.Muted) }
        }
        Box(Modifier.weight(1f)) {
            when (state.step) {
                0 -> Hello(Icons.Outlined.Lock, R.string.welcome_title, R.string.welcome_text)
                1 -> Favourites(state, actions)
                2 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Hello(Icons.Outlined.Wallpaper, R.string.welcome_wallpaper, R.string.welcome_wallpaper_text)
                    Choice(stringResource(R.string.use_wallpaper), state.wallpaper) { state.changeWallpaper(!state.wallpaper) }
                    TextButton(actions.wallpaper) { Text(stringResource(R.string.wallpaper_change), color = HomeColors.AccentText, fontSize = 17.sp) }
                }
                else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Hello(Icons.Outlined.Home, R.string.welcome_default, if (state.isDefault) R.string.default_yes else R.string.welcome_default_text)
                    if (!state.isDefault) TextButton(actions.makeDefault, Modifier.clip(CircleShape).background(HomeColors.Card)) {
                        Text(stringResource(R.string.make_default), color = HomeColors.Text, fontSize = 17.sp, modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${state.step + 1} / 4", Modifier.weight(1f), color = HomeColors.Muted)
            TextButton(state::nextStep, Modifier.clip(CircleShape).background(HomeColors.Accent)) {
                Text(stringResource(if (state.step < 3) R.string.next else R.string.lets_go), color = HomeColors.OnAccent, fontSize = 17.sp,
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }

@Composable private fun Hello(icon: ImageVector, title: Int, text: Int) = Column(Modifier.fillMaxWidth().padding(top = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Box(Modifier.size(72.dp).clip(CircleShape).background(HomeColors.Accent.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(36.dp), tint = HomeColors.Accent)
    }
    Text(stringResource(title), fontSize = 34.sp, fontWeight = FontWeight.Medium, lineHeight = 40.sp)
    Text(stringResource(text), fontSize = 17.sp, color = HomeColors.Muted)
}

@Composable private fun Choice(text: String, on: Boolean, onClick: () -> Unit) = Row(Modifier.fillMaxWidth().clip(CircleShape).clickable(onClick = onClick)
    .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(text, Modifier.weight(1f), fontSize = 17.sp)
    Checkbox(on, { onClick() }, colors = CheckboxDefaults.colors(checkedColor = HomeColors.Accent, checkmarkColor = HomeColors.OnAccent))
}

/** «Elige tus apps favoritas»: up to five for the dock, the family first. */
@Composable private fun Favourites(state: HomeState, actions: AppActions) = Column {
    Text(stringResource(R.string.welcome_favourites), Modifier.padding(top = 16.dp), fontSize = 30.sp, fontWeight = FontWeight.Medium, lineHeight = 36.sp)
    Text(stringResource(R.string.welcome_favourites_text, state.picked.size, Dock.MAX), Modifier.padding(vertical = 8.dp), color = HomeColors.Muted, fontSize = 16.sp)
    val list = (state.dockApps + Apps.family(state.apps) + state.drawer).distinctBy { it.key }
    LazyColumn {
        items(list, key = { it.key }) { app ->
            Row(Modifier.fillMaxWidth().clickable { state.togglePick(app) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                AppImage(app, actions, 44.dp)
                Text(app.label, Modifier.weight(1f).padding(start = 16.dp), fontSize = 17.sp)
                Checkbox(app.key in state.picked, { state.togglePick(app) },
                    colors = CheckboxDefaults.colors(checkedColor = HomeColors.Accent, checkmarkColor = HomeColors.OnAccent))
            }
        }
        item { Spacer(Modifier.padding(bottom = 8.dp)) }
    }
}
