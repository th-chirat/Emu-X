/*
 * PlayerViewModel.kt
 * -----------------------------------------------------------------------------
 * เตรียมทุกอย่างก่อนเริ่มเกม และจัดการ save/load
 *
 *   1) โหลดข้อมูลเกม + ค่าตั้งค่า (ความลื่น, overlay, ผังปุ่ม) จากฐานข้อมูล
 *   2) ติดตั้งไฟล์ระบบที่ core ต้องใช้ (เช่น assets ของ PPSSPP)
 *   3) หาวิธีส่งไฟล์เกมให้ core (GameFileResolver)
 *   4) จัดการไฟล์เซฟ: SRAM (เซฟในเกม) และ save state (เซฟทุกจุด 4 ช่อง)
 *
 * โครงสร้างโฟลเดอร์ในเครื่อง (ภายในแอพ ผู้ใช้อื่นมองไม่เห็น):
 *   files/system/                 ← BIOS + ไฟล์ระบบของ core
 *   files/saves/<เครื่อง>/        ← เซฟในเกม (.srm) และไฟล์ที่ core สร้าง
 *   files/states/<เครื่อง>/       ← save state (.state)
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.allemulator.core.data.GameRepository
import com.allemulator.core.data.SettingsRepository
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.emulation.ControllerLayout
import com.allemulator.core.emulation.GameSystem
import com.allemulator.core.emulation.OverlaySettings
import com.allemulator.core.emulation.PerformanceSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

/** สถานะของหน้าเล่นเกม */
sealed interface PlayerState {
    data object Loading : PlayerState
    data class Error(val message: String) : PlayerState
    data class Ready(
        val game: GameEntity,
        val system: GameSystem,
        val resolved: ResolvedGame,
        val performance: PerformanceSettings,
        val overlay: OverlaySettings,
        val layout: ControllerLayout,
        val haptics: Boolean,
        val sram: ByteArray?,
        val systemDir: File,
        val savesDir: File,
    ) : PlayerState
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val games: GameRepository,
    private val settings: SettingsRepository,
) : AndroidViewModel(app) {

    private val gameUri: String = checkNotNull(savedStateHandle[GameActivity.EXTRA_GAME_URI])

    /** true = รอบนี้เป็นการ retry หลังโหลดเกมแบบอื่นไม่สำเร็จ → ก๊อปไฟล์ลง cache */
    val forceCopy: Boolean = savedStateHandle[GameActivity.EXTRA_FORCE_COPY] ?: false
    private val _state = MutableStateFlow<PlayerState>(PlayerState.Loading)
    val state: StateFlow<PlayerState> = _state

    init {
        viewModelScope.launch { _state.value = prepare() }
    }

    private suspend fun prepare(): PlayerState = withContext(Dispatchers.IO) {
        runCatching {
            val game = games.game(gameUri) ?: return@withContext PlayerState.Error("ไม่พบเกมนี้ในรายการแล้ว")
            val system = GameSystem.fromId(game.systemId) ?: return@withContext PlayerState.Error("ไม่รู้จักเครื่องเกม ${game.systemId}")

            val systemDir = File(app.filesDir, "system").apply { mkdirs() }
            val savesDir = File(app.filesDir, "saves/${system.id}").apply { mkdirs() }
            SystemAssets.ensureInstalled(app, systemDir)

            games.markPlayed(game.uri)
            PlayerState.Ready(
                game = game,
                system = system,
                resolved = GameFileResolver(app).resolve(game, system, forceCopy),
                performance = settings.performance(system).first(),
                overlay = settings.overlay(system).first(),
                layout = settings.layout(system).first(),
                haptics = settings.haptics.first(),
                sram = sramFile(system, game).takeIf { it.exists() }?.readBytes(),
                systemDir = systemDir,
                savesDir = savesDir,
            )
        }.getOrElse { PlayerState.Error("เปิดเกมไม่ได้: ${it.message}") }
    }

    // ---------------- ไฟล์เซฟ ----------------

    /** ชื่อไฟล์เซฟอิงจาก hash ของ URI เกม (กันชื่อซ้ำ/อักขระแปลกๆ) */
    private fun key(game: GameEntity): String =
        MessageDigest.getInstance("SHA-1").digest(game.uri.toByteArray()).joinToString("") { "%02x".format(it) }.take(16)

    private fun sramFile(system: GameSystem, game: GameEntity) =
        File(app.filesDir, "saves/${system.id}/${key(game)}.srm")

    fun stateFile(slot: Int): File? {
        val ready = _state.value as? PlayerState.Ready ?: return null
        return File(app.filesDir, "states/${ready.system.id}/${key(ready.game)}_$slot.state").apply { parentFile?.mkdirs() }
    }

    /** บันทึกเซฟในเกม (SRAM) ลงไฟล์ — ถ้าข้อมูลว่างแปลว่าเกมนี้ไม่มีระบบเซฟ */
    suspend fun writeSram(bytes: ByteArray) = withContext(Dispatchers.IO) {
        val ready = _state.value as? PlayerState.Ready ?: return@withContext
        if (bytes.isNotEmpty()) sramFile(ready.system, ready.game).writeBytes(bytes)
    }

    override fun onCleared() {
        (_state.value as? PlayerState.Ready)?.resolved?.close()
    }
}
