/*
 * HomeScreen.kt
 * -----------------------------------------------------------------------------
 * หน้าแรกของแอพ (บรีฟข้อ 1.1)
 *
 *   ┌──────────────────────────────┐
 *   │ Emu-X           [⚙]  │  ← หัวหน้าจอ
 *   │ เล่นต่อ: [เกม][เกม][เกม] →   │  ← เกมล่าสุด (โชว์เมื่อเคยเล่น)
 *   │ ┌──────┐ ┌──────┐            │
 *   │ │ PSP  │ │ PS1  │  ...       │  ← การ์ดกระจกของแต่ละเครื่อง
 *   │ └──────┘ └──────┘            │
 *   └──────────────────────────────┘
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.home

import com.allemulator.core.designsystem.glass.CoverImage
import com.allemulator.core.designsystem.glass.BetaBadge
import com.allemulator.core.emulation.tr
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.designsystem.glass.GlassCard
import com.allemulator.core.designsystem.glass.GlassIconButton
import com.allemulator.core.designsystem.glass.GlassScaffold
import com.allemulator.core.designsystem.glass.GlassTopBar
import com.allemulator.core.designsystem.icons.ConsoleIcon
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.emulation.GameSystem

/**
 * @param onOpenSystem  กดการ์ดเครื่องเกม → ไปหน้ารายการเกม
 * @param onPlayGame    กดเกมในแถว "เล่นต่อ" → เข้าเกมเลย
 * @param onOpenSettings กดเฟือง → หน้าตั้งค่าแอพ
 */
@Composable
fun HomeScreen(
    onOpenSystem: (GameSystem) -> Unit,
    onPlayGame: (GameEntity) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val coverVersion by viewModel.coverVersion.collectAsStateWithLifecycle()

    GlassScaffold {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "Emu-X",
                subtitle = tr("เลือกเครื่องเกมที่อยากเล่น", "Pick a console to play"),
                actions = {
                    GlassIconButton(Icons.Rounded.Settings, tr("ตั้งค่า", "Settings"), onOpenSettings)
                },
            )

            LazyVerticalGrid(
                // การ์ดกว้างอย่างน้อย 140dp → มือถือได้ 2 คอลัมน์ (แม้จอแคบ ~340dp หรือตั้งตัวหนังสือ/การแสดงผลใหญ่), แท็บเล็ตได้ 4-5 คอลัมน์
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            ) {
                if (state.recent.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RecentRow(state.recent, onPlayGame, viewModel::coverFile, coverVersion)
                    }
                }
                items(state.cards, key = { it.system.id }) { card ->
                    SystemCard(card, onClick = { onOpenSystem(card.system) })
                }
            }
        }
    }
}

/** การ์ดกระจกของเครื่องเกม 1 เครื่อง */
@Composable
private fun SystemCard(card: SystemCardUi, onClick: () -> Unit) {
    val system = card.system
    GlassCard(
        onClick = onClick,
        tint = Color(system.accentStart),
        modifier = Modifier.fillMaxWidth().aspectRatio(0.86f),
    ) {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            ConsoleIcon(system, Modifier.size(96.dp).align(Alignment.Center).padding(bottom = 24.dp))
            if (system.isBeta) BetaBadge(Modifier.align(Alignment.TopEnd))
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(system.displayName, style = MaterialTheme.typography.titleLarge, color = GlassColors.TextPrimary)
                Text(
                    if (card.gameCount == 0) tr("ยังไม่มีเกม", "No games yet") else tr("${card.gameCount} เกม", if (card.gameCount == 1) "1 game" else "${card.gameCount} games"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassColors.TextSecondary,
                )
            }
        }
    }
}

/** แถว "เล่นต่อ" — เกมล่าสุดเลื่อนซ้าย-ขวาได้ */
@Composable
private fun RecentRow(
    games: List<GameEntity>,
    onPlay: (GameEntity) -> Unit,
    coverFile: (GameEntity) -> java.io.File?,
    coverVersion: Int,
) {
    Column {
        Text(
            tr("เล่นต่อ", "Continue"),
            style = MaterialTheme.typography.titleMedium,
            color = GlassColors.TextPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(games, key = { it.uri }) { game ->
                val system = GameSystem.fromId(game.systemId) ?: return@items
                GlassCard(
                    onClick = { onPlay(game) },
                    tint = Color(system.accentStart),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.width(200.dp),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        // ปกเกมที่ผู้ใช้ใส่ (กดค้างที่เกมในหน้ารายการ) — ไม่มีปก = ไอคอนเครื่องเกม
                        CoverImage(coverFile(game), coverVersion, Modifier.size(36.dp, 48.dp)) {
                            ConsoleIcon(system, Modifier.size(36.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(game.title, color = GlassColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(2.dp))
                            Text(system.displayName, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
