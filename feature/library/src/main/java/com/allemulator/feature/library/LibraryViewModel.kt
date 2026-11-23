/*
 * LibraryViewModel.kt
 * -----------------------------------------------------------------------------
 * "สมอง" ของหน้ารายการเกม (บรีฟข้อ 1.2)
 *   - โหลดเกมของเครื่องที่เลือก + กรองตามคำค้นหา
 *   - รับโฟลเดอร์ที่ผู้ใช้เลือกจากปุ่ม + แล้วสั่งสแกน
 *   - ติดดาว / เอาเกมออกจากรายการ / สแกนใหม่
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.library

import com.allemulator.core.emulation.tr
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allemulator.core.data.CoverStore
import com.allemulator.core.data.GameRepository
import com.allemulator.core.data.db.FolderEntity
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.emulation.GameSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val games: List<GameEntity> = emptyList(),
    val totalCount: Int = 0,
    val folders: List<FolderEntity> = emptyList(),
    val query: String = "",
    val isScanning: Boolean = false,
    val loaded: Boolean = false,
    /** ข้อความแจ้งเตือนสั้นๆ เช่น "เจอ 12 เกม" (null = ไม่มี) */
    val message: String? = null,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GameRepository,
    private val covers: CoverStore,
) : ViewModel() {

    // ---------------- ปกเกม (กดค้างที่เกม → เปลี่ยนปกเกม) ----------------

    /** เปลี่ยนเมื่อปกเกมใดๆ ถูกแก้ → รายการโหลดรูปใหม่ */
    val coverVersion: StateFlow<Int> = covers.version

    fun coverFile(game: GameEntity): java.io.File? = covers.coverFile(game.uri)

    fun setCover(game: GameEntity, image: Uri) = viewModelScope.launch {
        if (!covers.setCover(game.uri, image)) message.value = tr("ใส่ปกไม่สำเร็จ", "Couldn't set the cover")
    }

    fun removeCover(game: GameEntity) = covers.removeCover(game.uri)

    /** เครื่องเกมของหน้านี้ — ส่งมาจาก navigation ในชื่อ "systemId" */
    val system: GameSystem = GameSystem.fromId(checkNotNull(savedStateHandle.get<String>("systemId")))
        ?: GameSystem.NES

    private val query = MutableStateFlow("")
    private val scanning = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<LibraryUiState> = combine(
        repository.games(system),
        repository.folders(system),
        query,
        scanning,
        message,
    ) { games, folders, q, isScanning, msg ->
        val filtered = if (q.isBlank()) games else games.filter { it.title.contains(q, ignoreCase = true) }
        LibraryUiState(filtered, games.size, folders, q, isScanning, loaded = true, message = msg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onQueryChange(value: String) { query.value = value }

    /** ผู้ใช้เลือกโฟลเดอร์จากหน้าต่างระบบแล้ว */
    fun onFolderPicked(uri: Uri) = viewModelScope.launch {
        scanning.value = true
        message.value = runCatching { repository.addFolder(system, uri) }
            .fold(
                onSuccess = { n -> if (n == 0) tr("ไม่พบไฟล์เกม ${system.displayName} ในโฟลเดอร์นี้", "No ${system.displayName} games found in this folder") else tr("เพิ่ม $n เกมแล้ว", "Added $n games") },
                onFailure = { tr("อ่านโฟลเดอร์ไม่ได้: ${it.message}", "Can't read folder: ${it.message}") },
            )
        scanning.value = false
    }

    /** ผู้ใช้เลือกไฟล์ (หรือ .zip) จากหน้าต่างระบบแล้ว — zip จะถูกแตกไฟล์อัตโนมัติ */
    fun onFilesPicked(uris: List<Uri>) = viewModelScope.launch {
        scanning.value = true
        message.value = runCatching { repository.importFiles(system, uris) }
            .fold(
                onSuccess = { n -> if (n == 0) tr("ไม่พบไฟล์เกม ${system.displayName} ในไฟล์ที่เลือก", "No ${system.displayName} games in the selected files") else tr("เพิ่ม $n เกมแล้ว", "Added $n games") },
                onFailure = { tr("เปิดไฟล์ไม่ได้: ${it.message}", "Can't open file: ${it.message}") },
            )
        scanning.value = false
    }

    fun rescan() = viewModelScope.launch {
        scanning.value = true
        repository.rescanSystem(system)
        scanning.value = false
        message.value = tr("สแกนใหม่เรียบร้อย", "Rescan complete")
    }

    fun removeFolder(folder: FolderEntity) = viewModelScope.launch {
        repository.removeFolder(system, folder.treeUri)
    }

    fun toggleFavorite(game: GameEntity) = viewModelScope.launch { repository.toggleFavorite(game.uri) }
    fun hide(game: GameEntity) = viewModelScope.launch { repository.hide(game.uri) }
    fun consumeMessage() { message.value = null }
}
