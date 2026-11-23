/*
 * SystemSettingsScreen.kt
 * -----------------------------------------------------------------------------
 * ตั้งค่าเฉพาะเครื่องเกม (บรีฟข้อ 1.2.1) — เปิดจากปุ่มเฟืองในหน้ารายการเกม
 *
 *   ปุ่มควบคุม : แก้ไขตำแหน่ง/ขนาดปุ่ม, รีเซ็ต
 *   ความลื่น   : โหมด ประหยัดแบต / สมดุล / ภาพสวยสุด + ตั้งค่าขั้นสูง
 *   Overlay    : เลือกโชว์ FPS / CPU / RAM / อุณหภูมิ + มุมที่วาง
 *   BIOS       : นำเข้าไฟล์ BIOS (เฉพาะเครื่องที่ใช้)
 *
 * ทุกการเปลี่ยนแปลงบันทึกทันที และมีผลตอนเปิดเกมครั้งถัดไป
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.settings

import com.allemulator.core.emulation.tr
import com.allemulator.core.designsystem.settings.ChipChoice
import com.allemulator.core.designsystem.settings.NavRow
import com.allemulator.core.designsystem.settings.SwitchRow
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
        message.value = tr("รีเซ็ตผังปุ่มแล้ว", "Button layout reset")
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
                tr("นำเข้า $name แล้ว", "Imported $name")
            }.getOrElse { tr("นำเข้าไม่สำเร็จ: ${it.message}", "Import failed: ${it.message}") }
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
    val heavy = system in setOf(GameSystem.PSP, GameSystem.PS1)

    val pickBios = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importBios(uri)
    }

    GlassScaffold(tint = Color(system.accentStart)) {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = tr("ตั้งค่า ${system.displayName}", "${system.displayName} settings"),
                subtitle = message,
                navigation = { GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, tr("กลับ", "Back"), onBack) },
            )
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // ---------------- ปุ่มควบคุม ----------------
                GlassSection(tr("ปุ่มควบคุม", "Controls")) {
                    NavRow(tr("แก้ไขตำแหน่ง / ขนาดปุ่ม", "Edit button position / size"), { onEditLayout(system) }, tr("ลากย้ายปุ่ม ปรับขนาดและความโปร่งใส", "Drag buttons, change size and opacity"))
                    NavRow(tr("รีเซ็ตผังปุ่ม", "Reset button layout"), viewModel::resetLayout, tr("กลับไปใช้ตำแหน่งเริ่มต้น", "Go back to the default positions"))
                }

                // ---------------- ความลื่น ----------------
                GlassSection(tr("ความลื่นของเกม", "Game performance")) {
                    if (heavy) {
                        ChipChoice(
                            tr("โหมด", "Mode"),
                            PerformancePreset.entries.map { it to it.title },
                            perf.preset,
                        ) { p -> viewModel.updatePerformance { it.copy(preset = p, resolutionScale = null) } }
                        Text(perf.preset.description, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)

                        ChipChoice(
                            tr("ความละเอียดภาพ", "Resolution"),
                            listOf<Pair<Int?, String>>(null to tr("ตามโหมด", "By mode"), 1 to "1x", 2 to "2x", 3 to "3x", 4 to "4x"),
                            perf.resolutionScale,
                        ) { v -> viewModel.updatePerformance { it.copy(resolutionScale = v) } }

                        ChipChoice(
                            tr("ข้ามเฟรม (ช่วยให้ลื่นขึ้นแต่ภาพกระตุกเล็กน้อย)", "Frame skip (smoother, slightly choppy picture)"),
                            listOf(0 to tr("ปิด", "Close"), -1 to tr("อัตโนมัติ", "Auto"), 1 to "1", 2 to "2", 3 to "3"),
                            perf.frameSkip,
                        ) { v -> viewModel.updatePerformance { it.copy(frameSkip = v) } }

                        SwitchRow(
                            tr("เรนเดอร์แบบหลาย thread", "Multi-threaded rendering"),
                            perf.threadedRender,
                            { v -> viewModel.updatePerformance { it.copy(threadedRender = v) } },
                            tr("ใช้ CPU หลายคอร์ ลื่นขึ้นในมือถือส่วนใหญ่", "Uses several CPU cores, smoother on most phones"),
                        )
                        SwitchRow(
                            tr("ปรับความลื่นอัตโนมัติ", "Auto performance"),
                            perf.autoTune,
                            { v -> viewModel.updatePerformance { it.copy(autoTune = v) } },
                            tr("ถ้า FPS ต่ำกว่า 28 แอพจะลดความละเอียด/ข้ามเฟรมให้เองระหว่างเล่น", "If FPS drops below 28, the app lowers resolution / skips frames while you play"),
                        )
                        SwitchRow(
                            tr("โหมดเร่งความเร็ว (PSP)", "Speed hacks (PSP)"),
                            perf.speedHacks,
                            { v -> viewModel.updatePerformance { it.copy(speedHacks = v) } },
                            tr("เร็วขึ้นชัดเจน แต่บางเกมภาพ effect อาจเพี้ยนเล็กน้อย", "Noticeably faster, some effects may look slightly off"),
                        )
                    } else {
                        Text(
                            tr("${system.displayName} เป็นเครื่องเบา เล่นได้เต็มความเร็วในทุกมือถือ ไม่ต้องปรับ", "${system.displayName} is light and runs full speed on every phone — nothing to tune"),
                            color = GlassColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    SwitchRow(
                        tr("เสียงหน่วงต่ำ", "Low-latency audio"),
                        perf.lowLatencyAudio,
                        { v -> viewModel.updatePerformance { it.copy(lowLatencyAudio = v) } },
                        tr("ถ้าเสียงแตกหรือสะดุด ให้ปิดตัวนี้", "Turn off if the sound crackles or stutters"),
                    )
                    ChipChoice(
                        tr("ฟิลเตอร์ภาพ", "Screen filter"),
                        ScreenShader.entries.map { it to it.title },
                        perf.shader,
                    ) { v -> viewModel.updatePerformance { it.copy(shader = v) } }
                }

                // ---------------- Overlay ----------------
                GlassSection(tr("แสดงข้อมูลบนจอ (Overlay)", "On-screen info (overlay)")) {
                    SwitchRow(tr("FPS (เฟรมต่อวินาที)", "FPS (frames per second)"), overlay.showFps, { v -> viewModel.updateOverlay { it.copy(showFps = v) } })
                    SwitchRow("CPU", overlay.showCpu, { v -> viewModel.updateOverlay { it.copy(showCpu = v) } })
                    SwitchRow("RAM", overlay.showRam, { v -> viewModel.updateOverlay { it.copy(showRam = v) } })
                    SwitchRow(tr("อุณหภูมิแบตเตอรี่", "Battery temperature"), overlay.showBatteryTemp, { v -> viewModel.updateOverlay { it.copy(showBatteryTemp = v) } })
                    ChipChoice(
                        tr("ตำแหน่ง", "Position"),
                        OverlayCorner.entries.map { it to it.title },
                        overlay.corner,
                    ) { v -> viewModel.updateOverlay { it.copy(corner = v) } }
                }

                // ---------------- BIOS ----------------
                if (system.biosNote != null) {
                    GlassSection(tr("BIOS (ไม่บังคับ)", "BIOS (optional)")) {
                        Text(system.biosNote!!, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        NavRow(tr("นำเข้าไฟล์ BIOS", "Import BIOS file"), { pickBios.launch(arrayOf("*/*")) }, tr("ใช้ไฟล์ BIOS ที่คุณดัมพ์จากเครื่องของคุณเอง", "Use a BIOS you dumped from your own console"))
                    }
                }
            }
        }
    }
}
