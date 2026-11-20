/*
 * SystemSettingsScreen.kt
 * -----------------------------------------------------------------------------
 * ตั้งค่าเฉพาะเครื่องเกม (บรีฟข้อ 1.2.1) — เปิดจากปุ่มเฟืองในหน้ารายการเกม
 *
 *   ปุ่มควบคุม : แก้ไขตำแหน่ง/ขนาดปุ่ม, รีเซ็ต
 *   ความลื่น   : โหมด ประหยัดแบต / สมดุล / ภาพสวยสุด + ตั้งค่าขั้นสูง
 *   Overlay    : เลือกโชว์ FPS / CPU / GPU / RAM / อุณหภูมิ + มุมที่วาง
 *   BIOS       : นำเข้าไฟล์ BIOS (เฉพาะเครื่องที่ใช้)
 *
 * ทุกการเปลี่ยนแปลงบันทึกทันที และมีผลตอนเปิดเกมครั้งถัดไป
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.settings

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.allemulator.core.data.SettingsRepository
import com.allemulator.core.designsystem.glass.GlassIconButton
import com.allemulator.core.designsystem.glass.GlassScaffold
import com.allemulator.core.designsystem.glass.GlassSection
import com.allemulator.core.designsystem.glass.GlassTopBar
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.emulation.GameSystem
import com.allemulator.core.emulation.OverlayCorner
import com.allemulator.core.emulation.OverlaySettings
import com.allemulator.core.emulation.PerformancePreset
import com.allemulator.core.emulation.PerformanceSettings
import com.allemulator.core.emulation.ScreenShader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// =============================================================================
// ViewModel
// =============================================================================
@HiltViewModel
class SystemSettingsViewModel @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val settings: SettingsRepository,
) : AndroidViewModel(app) {

    val system: GameSystem = GameSystem.fromId(checkNotNull(savedStateHandle.get<String>("systemId"))) ?: GameSystem.NES

    val performance = settings.performance(system)
        .stateIn(viewModelScope, SharingStarted.Eagerly, PerformanceSettings())
    val overlay = settings.overlay(system)
        .stateIn(viewModelScope, SharingStarted.Eagerly, OverlaySettings())

    val message = MutableStateFlow<String?>(null)

    fun updatePerformance(change: (PerformanceSettings) -> PerformanceSettings) = viewModelScope.launch {
        settings.setPerformance(system, change(performance.value))
    }

    fun updateOverlay(change: (OverlaySettings) -> OverlaySettings) = viewModelScope.launch {
        settings.setOverlay(system, change(overlay.value))
    }

    fun resetLayout() = viewModelScope.launch {
        settings.resetLayout(system)
        message.value = "รีเซ็ตผังปุ่มแล้ว"
    }

    /** ก๊อปไฟล์ BIOS ที่ผู้ใช้เลือกไปไว้ในโฟลเดอร์ system ของแอพ (ใช้ชื่อไฟล์เดิม) */
    fun importBios(uri: Uri) = viewModelScope.launch {
        message.value = withContext(Dispatchers.IO) {
            runCatching {
                val resolver = app.contentResolver
                val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: "bios.bin"
                val target = File(app.filesDir, "system/$name").apply { parentFile?.mkdirs() }
                resolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } }
                "นำเข้า $name แล้ว"
            }.getOrElse { "นำเข้าไม่สำเร็จ: ${it.message}" }
        }
    }
}

// =============================================================================
// หน้าจอ
// =============================================================================
@Composable
fun SystemSettingsScreen(
    onBack: () -> Unit,
    onEditLayout: (GameSystem) -> Unit,
    viewModel: SystemSettingsViewModel = hiltViewModel(),
) {
    val system = viewModel.system
    val perf by viewModel.performance.collectAsStateWithLifecycle()
    val overlay by viewModel.overlay.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val heavy = system in setOf(GameSystem.PSP, GameSystem.PS1, GameSystem.PS2, GameSystem.NDS)

    val pickBios = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importBios(uri)
    }

    GlassScaffold(tint = Color(system.accentStart)) {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "ตั้งค่า ${system.displayName}",
                subtitle = message,
                navigation = { GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "กลับ", onBack) },
            )
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // ---------------- ปุ่มควบคุม ----------------
                GlassSection("ปุ่มควบคุม") {
                    NavRow("แก้ไขตำแหน่ง / ขนาดปุ่ม", { onEditLayout(system) }, "ลากย้ายปุ่ม ปรับขนาดและความโปร่งใส")
                    NavRow("รีเซ็ตผังปุ่ม", viewModel::resetLayout, "กลับไปใช้ตำแหน่งเริ่มต้น")
                }

                // ---------------- ความลื่น ----------------
                GlassSection("ความลื่นของเกม") {
                    if (heavy) {
                        ChipChoice(
                            "โหมด",
                            PerformancePreset.entries.map { it to it.title },
                            perf.preset,
                        ) { p -> viewModel.updatePerformance { it.copy(preset = p, resolutionScale = null) } }
                        Text(perf.preset.description, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)

                        ChipChoice(
                            "ความละเอียดภาพ",
                            listOf<Pair<Int?, String>>(null to "ตามโหมด", 1 to "1x", 2 to "2x", 3 to "3x", 4 to "4x"),
                            perf.resolutionScale,
                        ) { v -> viewModel.updatePerformance { it.copy(resolutionScale = v) } }

                        ChipChoice(
                            "ข้ามเฟรม (ช่วยให้ลื่นขึ้นแต่ภาพกระตุกเล็กน้อย)",
                            listOf(0 to "ปิด", -1 to "อัตโนมัติ", 1 to "1", 2 to "2", 3 to "3"),
                            perf.frameSkip,
                        ) { v -> viewModel.updatePerformance { it.copy(frameSkip = v) } }

                        SwitchRow(
                            "เรนเดอร์แบบหลาย thread",
                            perf.threadedRender,
                            { v -> viewModel.updatePerformance { it.copy(threadedRender = v) } },
                            "ใช้ CPU หลายคอร์ ลื่นขึ้นในมือถือส่วนใหญ่",
                        )
                    } else {
                        Text(
                            "${system.displayName} เป็นเครื่องเบา เล่นได้เต็มความเร็วในทุกมือถือ ไม่ต้องปรับ",
                            color = GlassColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    SwitchRow(
                        "เสียงหน่วงต่ำ",
                        perf.lowLatencyAudio,
                        { v -> viewModel.updatePerformance { it.copy(lowLatencyAudio = v) } },
                        "ถ้าเสียงแตกหรือสะดุด ให้ปิดตัวนี้",
                    )
                    ChipChoice(
                        "ฟิลเตอร์ภาพ",
                        ScreenShader.entries.map { it to it.title },
                        perf.shader,
                    ) { v -> viewModel.updatePerformance { it.copy(shader = v) } }
                }

                // ---------------- Overlay ----------------
                GlassSection("แสดงข้อมูลบนจอ (Overlay)") {
                    SwitchRow("FPS (เฟรมต่อวินาที)", overlay.showFps, { v -> viewModel.updateOverlay { it.copy(showFps = v) } })
                    SwitchRow("CPU", overlay.showCpu, { v -> viewModel.updateOverlay { it.copy(showCpu = v) } })
                    SwitchRow(
                        "GPU",
                        overlay.showGpu,
                        { v -> viewModel.updateOverlay { it.copy(showGpu = v) } },
                        "มือถือส่วนใหญ่ไม่อนุญาตให้อ่าน % GPU จะแสดง N/A พร้อมเวลาต่อเฟรมแทน",
                    )
                    SwitchRow("RAM", overlay.showRam, { v -> viewModel.updateOverlay { it.copy(showRam = v) } })
                    SwitchRow("อุณหภูมิแบตเตอรี่", overlay.showBatteryTemp, { v -> viewModel.updateOverlay { it.copy(showBatteryTemp = v) } })
                    ChipChoice(
                        "ตำแหน่ง",
                        OverlayCorner.entries.map { it to it.title },
                        overlay.corner,
                    ) { v -> viewModel.updateOverlay { it.copy(corner = v) } }
                }

                // ---------------- BIOS ----------------
                if (system.biosNote != null) {
                    GlassSection("BIOS (ไม่บังคับ)") {
                        Text(system.biosNote!!, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        NavRow("นำเข้าไฟล์ BIOS", { pickBios.launch(arrayOf("*/*")) }, "ใช้ไฟล์ BIOS ที่คุณดัมพ์จากเครื่องของคุณเอง")
                    }
                }
            }
        }
    }
}
