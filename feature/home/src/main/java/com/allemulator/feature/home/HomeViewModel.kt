/*
 * HomeViewModel.kt
 * -----------------------------------------------------------------------------
 * "สมอง" ของหน้าแรก: เตรียมข้อมูลที่หน้าจอต้องโชว์
 *   - การ์ดเครื่องเกมทั้ง 7 + จำนวนเกมของแต่ละเครื่อง
 *   - เกมที่เล่นล่าสุด (แถว "เล่นต่อ")
 * ViewModel อยู่รอดตอนหมุนจอ ข้อมูลจึงไม่หาย
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allemulator.core.data.GameRepository
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.emulation.GameSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** ข้อมูลการ์ด 1 ใบ */
data class SystemCardUi(val system: GameSystem, val gameCount: Int)

/** สถานะทั้งหมดของหน้าแรก */
data class HomeUiState(
    val cards: List<SystemCardUi> = GameSystem.entries.map { SystemCardUi(it, 0) },
    val recent: List<GameEntity> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: GameRepository,
) : ViewModel() {

    val state: StateFlow<HomeUiState> =
        combine(repository.counts(), repository.recent(limit = 10)) { counts, recent ->
            HomeUiState(
                cards = GameSystem.entries.map { SystemCardUi(it, counts[it] ?: 0) },
                recent = recent,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
