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

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

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
                onSuccess = { n -> if (n == 0) "ไม่พบไฟล์เกม ${system.displayName} ในโฟลเดอร์นี้" else "เพิ่ม $n เกมแล้ว" },
                onFailure = { "อ่านโฟลเดอร์ไม่ได้: ${it.message}" },
            )
        scanning.value = false
    }

    fun rescan() = viewModelScope.launch {
        scanning.value = true
        repository.rescanSystem(system)
        scanning.value = false
        message.value = "สแกนใหม่เรียบร้อย"
    }

    fun removeFolder(folder: FolderEntity) = viewModelScope.launch {
        repository.removeFolder(system, folder.treeUri)
    }

    fun toggleFavorite(game: GameEntity) = viewModelScope.launch { repository.toggleFavorite(game.uri) }
    fun hide(game: GameEntity) = viewModelScope.launch { repository.hide(game.uri) }
    fun consumeMessage() { message.value = null }
}
