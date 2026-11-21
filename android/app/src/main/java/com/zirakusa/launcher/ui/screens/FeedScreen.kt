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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.zirakusa.launcher.data.CardKind
import com.zirakusa.launcher.data.Cards
import com.zirakusa.launcher.ui.Page
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

/** «Hoy», left of the home screen: the greeting and the cards in the order of «Editar esta vista» (live or preview). */
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
    val live = state.cards.associateBy { it.app.pkg }
    state.slots.filter { it.visible }.forEach { slot ->
        val hide = { state.showCard(slot.id, false) }
        val edit = { state.editCards(Page.FEED) }
        when (val kind = CardKind.of(slot.id)) {
            CardKind.FREQUENT -> AppsCard(stringResource(R.string.frequent), state.frequent, stringResource(R.string.frequent_empty), state, actions, hide, edit)
            CardKind.RECENT -> AppsCard(stringResource(R.string.recent), state.recentApps.take(4), stringResource(R.string.frequent_empty), state, actions, hide, edit)
            else -> {
                val pkg = Cards.pkgOf(slot.id)
                val card = pkg?.let { live[it] }
                if (card != null) CardView(card, actions, hide, edit)
                else PreviewCard(kind, state.apps.firstOrNull { it.pkg == pkg }?.label ?: slot.id, hide, edit)
            }
        }
    }
    androidx.compose.material3.TextButton(edit@{ state.editCards(Page.FEED) }, Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(50))
        .background(HomeColors.Accent)) { Text(stringResource(R.string.edit_view), color = HomeColors.OnAccent, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp)) }
    Spacer(Modifier.padding(bottom = 16.dp))
}

/** The «⋯» of every card: hide it, or edit the whole view. */
@Composable private fun CardMenu(onHide: () -> Unit, onEdit: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton({ open = true }, Modifier.size(32.dp)) { Icon(Icons.Outlined.MoreHoriz, stringResource(R.string.card_options), tint = HomeColors.Muted) }
        DropdownMenu(open, { open = false }) {
            DropdownMenuItem({ Text(stringResource(R.string.hide_card)) }, { open = false; onHide() })
            DropdownMenuItem({ Text(stringResource(R.string.edit_view)) }, { open = false; onEdit() })
        }
    }
}

@Composable private fun Header(title: String, onHide: () -> Unit, onEdit: () -> Unit, lead: @Composable () -> Unit = {}) =
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        lead()
        Text(title, Modifier.weight(1f), fontSize = 17.sp, color = HomeColors.Text, fontWeight = FontWeight.Medium)
        CardMenu(onHide, onEdit)
    }

/**
 * A card whose app is missing or gives no summary yet: what it will show, greyed, with «Vista previa». Notas and
 * Documentos (Filem) are previews until those apps give their summary.
 */
@Composable private fun PreviewCard(kind: CardKind?, label: String, onHide: () -> Unit, onEdit: () -> Unit) =
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(HomeColors.Card).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Header(stringResource(cardTitles[kind] ?: R.string.card_app).format(label), onHide, onEdit)
        Text(stringResource(R.string.preview), Modifier.clip(RoundedCornerShape(50)).background(HomeColors.Chip).padding(horizontal = 10.dp, vertical = 3.dp),
            color = HomeColors.AccentText, fontSize = 12.sp)
        repeat(3) { i ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(HomeColors.Chip))
                Box(Modifier.padding(start = 12.dp).fillMaxWidth(listOf(0.7f, 0.55f, 0.62f)[i]).height(12.dp).clip(RoundedCornerShape(6.dp)).background(HomeColors.Chip))
            }
        }
        Text(stringResource(previewTexts[kind] ?: R.string.preview_app), color = HomeColors.Muted, fontSize = 14.sp)
    }

val cardTitles = mapOf(CardKind.CLOCK to R.string.card_clock, CardKind.CALENDAR to R.string.card_calendar, CardKind.NOTES to R.string.card_notes,
    CardKind.DOCUMENTS to R.string.card_documents, CardKind.FREQUENT to R.string.frequent, CardKind.RECENT to R.string.recent)
private val previewTexts = mapOf(CardKind.CLOCK to R.string.preview_clock, CardKind.CALENDAR to R.string.preview_calendar,
    CardKind.NOTES to R.string.preview_notes, CardKind.DOCUMENTS to R.string.preview_documents)

/** A family app's card: its icon and title, then its lines (or what it says when there is nothing); tap opens the app. */
@Composable private fun CardView(card: FamilyCard, actions: AppActions, onHide: () -> Unit, onEdit: () -> Unit) = Column(Modifier.fillMaxWidth()
    .clip(RoundedCornerShape(24.dp)).background(HomeColors.Card).clickable { actions.open(card.app) }.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Header(card.title, onHide, onEdit) { AppImage(card.app, actions, 28.dp); Spacer(Modifier.size(12.dp)) }
    if (card.lines.isEmpty()) Text(card.empty, color = HomeColors.Muted, fontSize = 15.sp)
    card.lines.forEach { Text(it, fontSize = 16.sp) }
}

@Composable private fun AppsCard(title: String, apps: List<AppEntry>, empty: String, state: HomeState, actions: AppActions, onHide: () -> Unit,
    onEdit: () -> Unit) = Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(HomeColors.Card).padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Header(title, onHide, onEdit)
    if (apps.isEmpty()) Text(empty, color = HomeColors.Muted, fontSize = 15.sp)
    apps.chunked(4).forEach { row ->
        Row(Modifier.fillMaxWidth()) {
            row.forEach { AppCell(it, actions, state, Modifier.weight(1f), size = 48.dp) }
            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}
