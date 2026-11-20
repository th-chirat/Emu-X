/*
 * LibraryScreen.kt
 * -----------------------------------------------------------------------------
 * หน้ารายการเกมของเครื่องที่เลือก (บรีฟข้อ 1.2 และ 1.2.1)
 *
 *   [←] PlayStation Portable        [⟳][📁][⚙]
 *   ( ค้นหาเกม... )
 *   ┌ เกม A                      ★ ┐
 *   ┌ เกม B                        ┐
 *                                  (+)   ← ปุ่มเพิ่มโฟลเดอร์
 *
 * ถ้ายังไม่มีเกม: โชว์ไอคอนเครื่อง + ปุ่ม "เลือกโฟลเดอร์เกม"
 * กดค้างที่เกม: เมนู ติดดาว / เอาออกจากรายการ
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.designsystem.glass.GlassButton
import com.allemulator.core.designsystem.glass.GlassFab
import com.allemulator.core.designsystem.glass.GlassIconButton
import com.allemulator.core.designsystem.glass.GlassScaffold
import com.allemulator.core.designsystem.glass.GlassSearchField
import com.allemulator.core.designsystem.glass.GlassTopBar
import com.allemulator.core.designsystem.glass.glass
import com.allemulator.core.designsystem.icons.ConsoleIcon
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.emulation.GameSystem
import kotlinx.coroutines.delay

@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onPlay: (GameEntity) -> Unit,
    onOpenSystemSettings: (GameSystem) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val system = viewModel.system
    var showFolders by remember { mutableStateOf(false) }

    // หน้าต่างเลือกโฟลเดอร์ของระบบ Android (SAF)
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) viewModel.onFolderPicked(uri)
    }

    // ข้อความแจ้งเตือนหายเองใน 3 วินาที
    LaunchedEffect(state.message) {
        if (state.message != null) { delay(3_000); viewModel.consumeMessage() }
    }

    GlassScaffold(tint = Color(system.accentStart)) {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = system.displayName,
                subtitle = "${system.fullName} • ${state.totalCount} เกม",
                navigation = { GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "กลับ", onBack) },
                actions = {
                    GlassIconButton(Icons.Rounded.Refresh, "สแกนใหม่", viewModel::rescan)
                    GlassIconButton(Icons.Rounded.Folder, "โฟลเดอร์", { showFolders = true })
                    GlassIconButton(Icons.Rounded.Settings, "ตั้งค่า ${system.displayName}", { onOpenSystemSettings(system) })
                },
            )
            if (state.isScanning) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
            if (system.isBeta) BetaWarning(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

            when {
                !state.loaded -> Unit
                state.totalCount == 0 -> EmptyState(system, onAdd = { pickFolder.launch(null) })
                else -> {
                    GlassSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "ค้นหาเกม...",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.games, key = { it.uri }) { game ->
                            GameRow(
                                game = game,
                                system = system,
                                onPlay = { onPlay(game) },
                                onToggleFavorite = { viewModel.toggleFavorite(game) },
                                onHide = { viewModel.hide(game) },
                            )
                        }
                    }
                }
            }
        }

        // ปุ่ม + (บรีฟข้อ 1.2) — โชว์เสมอ กดเพื่อเลือกโฟลเดอร์เกม (เพิ่มได้หลายโฟลเดอร์)
        GlassFab(
            Icons.Rounded.Add,
            "เพิ่มโฟลเดอร์เกม",
            onClick = { pickFolder.launch(null) },
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(24.dp),
        )

        state.message?.let { msg ->
            Text(
                msg,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 100.dp)
                    .glass(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    if (showFolders) {
        FoldersDialog(
            folders = state.folders.map { it.treeUri },
            onRemove = { uri -> state.folders.firstOrNull { it.treeUri == uri }?.let(viewModel::removeFolder) },
            onAdd = { showFolders = false; pickFolder.launch(null) },
            onDismiss = { showFolders = false },
        )
    }
}

/** หน้าว่าง (ยังไม่มีเกม): ไอคอนใหญ่ + คำอธิบาย + ปุ่มเลือกโฟลเดอร์ */
@Composable
private fun EmptyState(system: GameSystem, onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ConsoleIcon(system, Modifier.size(140.dp))
        Spacer(Modifier.height(24.dp))
        Text("ยังไม่มีเกม ${system.displayName}", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "กดปุ่มด้านล่างแล้วเลือกโฟลเดอร์ที่คุณเก็บไฟล์เกมไว้\nรองรับ: " +
                system.extensions.joinToString(" ") { ".$it" },
            color = GlassColors.TextSecondary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        system.biosNote?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = GlassColors.TextSecondary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(24.dp))
        GlassButton("เลือกโฟลเดอร์เกม", onAdd, icon = Icons.Rounded.CreateNewFolder, tint = Color(system.accentStart))
    }
}

/** แถวเกม 1 เกม: แตะ = เล่น, กดค้าง = เมนู */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameRow(
    game: GameEntity,
    system: GameSystem,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHide: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(20.dp))
                .combinedClickable(onClick = onPlay, onLongClick = { menu = true })
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConsoleIcon(system, Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(game.title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${game.fileName}  •  ${formatSize(game.sizeBytes)}",
                    color = GlassColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (game.favorite) Icon(Icons.Rounded.Star, "ติดดาว", tint = GlassColors.Beta)
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text(if (game.favorite) "เลิกติดดาว" else "ติดดาว") },
                onClick = { menu = false; onToggleFavorite() },
            )
            DropdownMenuItem(
                text = { Text("เอาออกจากรายการ (ไม่ลบไฟล์)") },
                onClick = { menu = false; onHide() },
            )
        }
    }
}

/** แถบเตือน PS2 Beta */
@Composable
private fun BetaWarning(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(16.dp), tint = GlassColors.Beta)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.WarningAmber, null, tint = GlassColors.Beta)
        Spacer(Modifier.width(10.dp))
        Text(
            "PS2 ยังเป็นรุ่นทดลอง ต้องใช้มือถือสเปคสูง หลายเกมอาจช้าหรือเล่นไม่ได้",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** หน้าต่างจัดการโฟลเดอร์ */
@Composable
private fun FoldersDialog(folders: List<String>, onRemove: (String) -> Unit, onAdd: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("โฟลเดอร์เกม") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (folders.isEmpty()) Text("ยังไม่ได้เลือกโฟลเดอร์")
                folders.forEach { uri ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            Uri.decode(uri.substringAfterLast("/tree/")).replace("primary:", "ที่เก็บข้อมูลภายใน/"),
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(onClick = { onRemove(uri) }) { Icon(Icons.Rounded.Close, "เอาโฟลเดอร์ออก") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onAdd) { Text("เพิ่มโฟลเดอร์") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ปิด") } },
    )
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1L shl 30 -> "%.1f GB".format(bytes / (1L shl 30).toDouble())
    bytes >= 1L shl 20 -> "%.1f MB".format(bytes / (1L shl 20).toDouble())
    else -> "${bytes / 1024} KB"
}
