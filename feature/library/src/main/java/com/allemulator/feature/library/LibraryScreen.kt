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

import com.allemulator.core.designsystem.settings.BiosGuideDialog
import com.allemulator.core.emulation.tr
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import com.allemulator.core.designsystem.glass.CoverImage
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
    var showAddChoice by remember { mutableStateOf(false) }
    val coverVersion by viewModel.coverVersion.collectAsStateWithLifecycle()

    // หน้าต่างเลือกรูปของระบบ (Photo Picker — ไม่ต้องขอสิทธิ์อ่านรูปทั้งเครื่อง) ใช้ใส่ปกเกม
    var coverTarget by remember { mutableStateOf<GameEntity?>(null) }
    val pickCover = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        val game = coverTarget
        if (uri != null && game != null) viewModel.setCover(game, uri)
        coverTarget = null
    }

    // หน้าต่างเลือกโฟลเดอร์ของระบบ Android (SAF)
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) viewModel.onFolderPicked(uri)
    }
    // หน้าต่างเลือก "ไฟล์" (เลือกได้หลายไฟล์) — รองรับ .zip ที่จะถูกแตกไฟล์ให้อัตโนมัติ
    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.onFilesPicked(uris)
    }

    // ข้อความแจ้งเตือนหายเองใน 3 วินาที
    LaunchedEffect(state.message) {
        if (state.message != null) { delay(3_000); viewModel.consumeMessage() }
    }

    GlassScaffold(tint = Color(system.accentStart)) {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = if (system.isBeta) "${system.displayName} (Beta)" else system.displayName,
                subtitle = tr("${system.fullName} • ${state.totalCount} เกม", "${system.fullName} • ${state.totalCount} " + if (state.totalCount == 1) "game" else "games"),
                navigation = { GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, tr("กลับ", "Back"), onBack) },
                actions = {
                    GlassIconButton(Icons.Rounded.Refresh, tr("สแกนใหม่", "Rescan"), viewModel::rescan)
                    GlassIconButton(Icons.Rounded.Folder, tr("โฟลเดอร์", "Folders"), { showFolders = true })
                    GlassIconButton(Icons.Rounded.Settings, tr("ตั้งค่า ${system.displayName}", "${system.displayName} settings"), { onOpenSystemSettings(system) })
                },
            )
            if (state.isScanning) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }

            when {
                !state.loaded -> Unit
                state.totalCount == 0 -> EmptyState(
                    system,
                    onAddFolder = { pickFolder.launch(null) },
                    onAddFiles = { pickFiles.launch(arrayOf("*/*")) },
                )
                else -> {
                    GlassSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = tr("ค้นหาเกม...", "Search games..."),
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
                                cover = viewModel.coverFile(game),
                                coverVersion = coverVersion,
                                onPlay = { onPlay(game) },
                                onToggleFavorite = { viewModel.toggleFavorite(game) },
                                onHide = { viewModel.hide(game) },
                                onChangeCover = {
                                    coverTarget = game
                                    pickCover.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                onRemoveCover = { viewModel.removeCover(game) },
                            )
                        }
                    }
                }
            }
        }

        // ปุ่ม + (บรีฟข้อ 1.2) กดแล้วเลือกได้ว่าจะเพิ่ม "โฟลเดอร์" หรือ "ไฟล์/zip"
        // ซ่อนตอนยังไม่มีเกม เพราะหน้าว่างมีปุ่มเพิ่มเกม 2 ปุ่มอยู่แล้ว (ไม่ให้ซ้ำ — บรีฟรอบ 4 ข้อ 4)
        if (state.loaded && state.totalCount > 0) {
            GlassFab(
                Icons.Rounded.Add,
                tr("เพิ่มเกม", "Add games"),
                onClick = { showAddChoice = true },
                modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(24.dp),
            )
        }

        state.message?.let { msg ->
            Text(
                msg,
                color = GlassColors.TextPrimary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 100.dp)
                    .glass(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    if (showAddChoice) {
        AlertDialog(
            onDismissRequest = { showAddChoice = false },
            title = { Text(tr("เพิ่มเกม ${system.displayName}", "Add ${system.displayName} games")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassButton(
                        tr("เลือกโฟลเดอร์เกม", "Choose game folder"),
                        { showAddChoice = false; pickFolder.launch(null) },
                        icon = Icons.Rounded.CreateNewFolder, tint = Color(system.accentStart),
                    )
                    GlassButton(
                        tr("เลือกไฟล์เกม / .zip", "Choose game file / .zip"),
                        { showAddChoice = false; pickFiles.launch(arrayOf("*/*")) },
                        icon = Icons.Rounded.Add, tint = Color(system.accentStart),
                    )
                    Text(tr("ไฟล์ .zip จะถูกแตกไฟล์ให้อัตโนมัติ", ".zip files are extracted automatically"), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { showAddChoice = false }) { Text(tr("ยกเลิก", "Cancel")) } },
        )
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
private fun EmptyState(system: GameSystem, onAddFolder: () -> Unit, onAddFiles: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ConsoleIcon(system, Modifier.size(140.dp))
        Spacer(Modifier.height(24.dp))
        Text(tr("ยังไม่มีเกม ${system.displayName}", "No ${system.displayName} games yet"), style = MaterialTheme.typography.titleLarge, color = GlassColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            tr("เลือกโฟลเดอร์ที่เก็บเกม หรือเลือกไฟล์เกม/.zip ทีละไฟล์\nรองรับ: ", "Choose the folder with your games, or pick game/.zip files\nSupported: ") +
                system.extensions.joinToString(" ") { ".$it" },
            color = GlassColors.TextSecondary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        system.biosNote?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = GlassColors.TextSecondary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        }
        if (system.biosRequired) {
            var guide by remember { mutableStateOf(false) }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { guide = true }) { Text(tr("วิธีติดตั้ง BIOS", "How to install the BIOS")) }
            if (guide) BiosGuideDialog(system, onDismiss = { guide = false })
        }
        Spacer(Modifier.height(24.dp))
        GlassButton(tr("เลือกโฟลเดอร์เกม", "Choose game folder"), onAddFolder, icon = Icons.Rounded.CreateNewFolder, tint = Color(system.accentStart))
        Spacer(Modifier.height(12.dp))
        GlassButton(tr("เลือกไฟล์เกม / .zip", "Choose game file / .zip"), onAddFiles, icon = Icons.Rounded.Add, tint = Color(system.accentStart))
    }
}

/** แถวเกม 1 เกม: แตะ = เล่น, กดค้าง = เมนู */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameRow(
    game: GameEntity,
    system: GameSystem,
    cover: java.io.File?,
    coverVersion: Int,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHide: () -> Unit,
    onChangeCover: () -> Unit,
    onRemoveCover: () -> Unit,
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
            // ปกเกมที่ผู้ใช้ใส่เอง (แนวตั้งแบบกล่องเกม) — ไม่มีปก = ไอคอนเครื่องเกม
            CoverImage(cover, coverVersion, Modifier.size(width = 48.dp, height = 64.dp)) {
                ConsoleIcon(system, Modifier.size(44.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(game.title, color = GlassColors.TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${game.fileName}  •  ${formatSize(game.sizeBytes)}",
                    color = GlassColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (game.favorite) Icon(Icons.Rounded.Star, tr("ติดดาว", "Favorite"), tint = GlassColors.Beta)
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text(if (game.favorite) tr("เลิกติดดาว", "Unfavorite") else tr("ติดดาว", "Favorite")) },
                onClick = { menu = false; onToggleFavorite() },
            )
            DropdownMenuItem(
                text = { Text(tr("เปลี่ยนปกเกม", "Change cover")) },
                onClick = { menu = false; onChangeCover() },
            )
            if (cover != null) {
                DropdownMenuItem(
                    text = { Text(tr("ลบปกเกม", "Remove cover")) },
                    onClick = { menu = false; onRemoveCover() },
                )
            }
            DropdownMenuItem(
                text = { Text(tr("เอาออกจากรายการ (ไม่ลบไฟล์)", "Remove from list (keeps the file)")) },
                onClick = { menu = false; onHide() },
            )
        }
    }
}

/** หน้าต่างจัดการโฟลเดอร์ */
@Composable
private fun FoldersDialog(folders: List<String>, onRemove: (String) -> Unit, onAdd: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("โฟลเดอร์เกม", "Game folders")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (folders.isEmpty()) Text(tr("ยังไม่ได้เลือกโฟลเดอร์", "No folders yet"))
                folders.forEach { uri ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            Uri.decode(uri.substringAfterLast("/tree/")).replace("primary:", tr("ที่เก็บข้อมูลภายใน/", "Internal storage/")),
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(onClick = { onRemove(uri) }) { Icon(Icons.Rounded.Close, tr("เอาโฟลเดอร์ออก", "Remove folder")) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onAdd) { Text(tr("เพิ่มโฟลเดอร์", "Add folder")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("ปิด", "Close")) } },
    )
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1L shl 30 -> "%.1f GB".format(bytes / (1L shl 30).toDouble())
    bytes >= 1L shl 20 -> "%.1f MB".format(bytes / (1L shl 20).toDouble())
    else -> "${bytes / 1024} KB"
}
