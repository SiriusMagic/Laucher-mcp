package com.zirakusa.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.launcher.R
import com.zirakusa.launcher.data.CardKind
import com.zirakusa.launcher.data.CardStatus
import com.zirakusa.launcher.data.Cards
import com.zirakusa.launcher.mcp.McpMode
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeColors

/** «Editar esta vista»: every card of «Hoy» with its switch and arrows to order it; live or preview; your AI can do the same by MCP. */
@Composable fun CardsScreen(state: HomeState) = Column(Modifier.fillMaxSize().background(HomeColors.Background).systemBarsPadding()
    .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
    PageTop(stringResource(R.string.edit_view)) { state.page = state.cardsBack }
    Text(stringResource(R.string.edit_view_hint), Modifier.padding(start = 4.dp, bottom = 12.dp), color = HomeColors.Muted, fontSize = 15.sp)
    Group {
        state.slots.forEachIndexed { i, slot ->
            if (i > 0) Divider()
            val kind = CardKind.of(slot.id)
            val label = Cards.pkgOf(slot.id)?.let { p -> state.apps.firstOrNull { it.pkg == p }?.label } ?: slot.id
            val live = Cards.status(slot.id, state.livePackages) == CardStatus.LIVE
            Row(Modifier.fillMaxWidth().clickable { state.showCard(slot.id, !slot.visible) }.padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    IconButton({ state.moveCard(slot.id, -1) }, enabled = i > 0) { Icon(Icons.Outlined.KeyboardArrowUp, stringResource(R.string.move_up)) }
                    IconButton({ state.moveCard(slot.id, 1) }, enabled = i < state.slots.size - 1) { Icon(Icons.Outlined.KeyboardArrowDown, stringResource(R.string.move_down)) }
                }
                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(stringResource(cardTitles[kind] ?: R.string.card_app).format(label), fontSize = 17.sp)
                    Text(stringResource(if (live) R.string.live else R.string.preview), color = if (live) HomeColors.AccentText else HomeColors.Muted, fontSize = 13.sp)
                }
                Checkbox(slot.visible, { state.showCard(slot.id, it) },
                    colors = CheckboxDefaults.colors(checkedColor = HomeColors.Accent, checkmarkColor = HomeColors.OnAccent))
            }
        }
    }
    Group(Modifier.padding(vertical = 12.dp)) {
        Item(stringResource(R.string.mcp_title), stringResource(if (McpMode.active) R.string.mcp_cards_on else R.string.mcp_cards_off)) { state.page = Page.MCP }
    }
}
