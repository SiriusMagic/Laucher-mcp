package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.FamilyCard
import com.zirakusa.launcher.data.Greeting
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.theme.HomeColors
import java.time.format.DateTimeFormatter

/** «Hoy», left of the home screen: the greeting, a card per family app (Reloj, Calendario…), frequent and recent apps. */
@Composable fun FeedScreen(state: HomeState, actions: AppActions) = Column(Modifier.fillMaxSize().background(HomeColors.Background).systemBarsPadding()
    .pointerInput(Unit) { var total = 0f; detectHorizontalDragGestures(onDragStart = { total = 0f },
        onDragEnd = { if (total < -60.dp.toPx()) state.home() }) { _, dx -> total += dx } }
    .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(Modifier.fillMaxWidth().padding(top = 16.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(when (state.greeting) { Greeting.MORNING -> R.string.good_morning; Greeting.AFTERNOON -> R.string.good_afternoon
                Greeting.NIGHT -> R.string.good_night }), fontSize = 28.sp, fontWeight = FontWeight.Medium)
            val l = locale()
            Text(DateTimeFormatter.ofPattern(if (l.language == "es") "EEEE, d 'de' MMMM" else "EEEE, MMMM d", l).format(state.now).replaceFirstChar { it.titlecase() },
                color = HomeColors.Muted, fontSize = 16.sp)
        }
        IconButton(state::home) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, stringResource(R.string.back_home), tint = HomeColors.Text) }
    }
    LaunchedEffect(state.apps) { state.cards = withContext(Dispatchers.IO) { actions.cards(state.apps) } }
    state.cards.forEach { CardView(it, actions) }
    if (state.cards.isEmpty()) Card(stringResource(R.string.today_family)) {
        Text(stringResource(R.string.today_family_hint), color = HomeColors.Muted, fontSize = 15.sp)
    }
    AppsCard(stringResource(R.string.frequent), state.frequent, stringResource(R.string.frequent_empty), state, actions)
    AppsCard(stringResource(R.string.recent), state.recentApps.take(4), stringResource(R.string.frequent_empty), state, actions)
    Spacer(Modifier.padding(bottom = 16.dp))
}

@Composable private fun Card(title: String, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) =
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(HomeColors.Card)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontSize = 17.sp, color = HomeColors.Text, fontWeight = FontWeight.Medium)
        content()
    }

/** A family app's card: its icon and title, then its lines (or what it says when there is nothing); tap opens the app. */
@Composable private fun CardView(card: FamilyCard, actions: AppActions) = Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
    .background(HomeColors.Card).clickable { actions.open(card.app) }.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppImage(card.app, actions, 28.dp)
        Text(card.title, Modifier.padding(start = 12.dp), fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
    if (card.lines.isEmpty()) Text(card.empty, color = HomeColors.Muted, fontSize = 15.sp)
    card.lines.forEach { Text(it, fontSize = 16.sp) }
}

@Composable private fun AppsCard(title: String, apps: List<AppEntry>, empty: String, state: HomeState, actions: AppActions) = Card(title) {
    if (apps.isEmpty()) Text(empty, color = HomeColors.Muted, fontSize = 15.sp)
    apps.chunked(4).forEach { row ->
        Row(Modifier.fillMaxWidth()) {
            row.forEach { AppCell(it, actions, state, Modifier.weight(1f), size = 48.dp) }
            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}
