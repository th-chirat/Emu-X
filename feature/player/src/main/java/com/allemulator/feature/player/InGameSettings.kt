/*
 * InGameSettings.kt
 * -----------------------------------------------------------------------------
 * หน้า "ตั้งค่าระหว่างเล่น" (อยู่ในเมนูหยุดเกม → ตั้งค่า)
 * หน้าตาเหมือนหน้าตั้งค่าเครื่อง (ใช้ชิ้นส่วนชุดเดียวกัน: ChipChoice / SwitchRow)
 *
 * ปรับได้ทันทีโดยไม่ต้องออกจากเกม:
 *   - โหมดความลื่น (ประหยัดแบต / สมดุล / ภาพสวยสุด) + ปรับความลื่นอัตโนมัติ
 *     (บางค่า เช่น ความละเอียดของ PSP อาจมีผลเต็มที่หลังเปิดเกมใหม่ — เป็นข้อจำกัดของ core)
 *   - ฟิลเตอร์ภาพ (ปกติ / คมชัด / LCD / CRT)
 *   - ข้อมูลบนจอ: FPS / CPU / RAM / อุณหภูมิแบต
 * ทุกค่าถูกบันทึกเป็นค่าตั้งค่าของเครื่องเกมนั้นด้วย
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.allemulator.core.designsystem.settings.ChipChoice
import com.allemulator.core.designsystem.settings.SwitchRow
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.emulation.OverlaySettings
import com.allemulator.core.emulation.PerformancePreset
import com.allemulator.core.emulation.PerformanceSettings
import com.allemulator.core.emulation.ScreenShader
import com.allemulator.core.emulation.tr

@Composable
fun InGameSettings(
    performance: PerformanceSettings,
    overlay: OverlaySettings,
    onPerformance: (PerformanceSettings) -> Unit,
    onOverlay: (OverlaySettings) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Section(tr("ความลื่นของเกม", "Game performance"))
        ChipChoice(
            tr("โหมด", "Mode"),
            PerformancePreset.entries.map { it to it.title },
            performance.preset,
        ) { v -> onPerformance(performance.copy(preset = v, resolutionScale = null)) } // เลือกโหมด = ล้างค่าที่ปรับเอง
        Text(performance.preset.description, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        SwitchRow(
            tr("ปรับความลื่นอัตโนมัติ", "Auto performance"),
            performance.autoTune,
            { onPerformance(performance.copy(autoTune = it)) },
            tr("ถ้า FPS ต่ำกว่า 28 แอพจะลดความละเอียด/ข้ามเฟรมให้เอง", "If FPS drops below 28, lowers resolution / skips frames for you"),
        )
        ChipChoice(
            tr("ฟิลเตอร์ภาพ", "Screen filter"),
            ScreenShader.entries.map { it to it.title },
            performance.shader,
        ) { v -> onPerformance(performance.copy(shader = v)) }

        Section(tr("แสดงข้อมูลบนจอ (Overlay)", "On-screen info (overlay)"))
        SwitchRow(tr("FPS (เฟรมต่อวินาที)", "FPS (frames per second)"), overlay.showFps, { onOverlay(overlay.copy(showFps = it)) })
        SwitchRow("CPU", overlay.showCpu, { onOverlay(overlay.copy(showCpu = it)) })
        SwitchRow("RAM", overlay.showRam, { onOverlay(overlay.copy(showRam = it)) })
        SwitchRow(tr("อุณหภูมิแบตเตอรี่", "Battery temperature"), overlay.showBatteryTemp, { onOverlay(overlay.copy(showBatteryTemp = it)) })
    }
}

/** หัวข้อกลุ่ม (สีฟ้า เหมือนหัวข้อในหน้าตั้งค่าเครื่อง) */
@Composable
private fun Section(text: String) {
    Text(text, color = GlassColors.Accent, style = MaterialTheme.typography.titleMedium)
}
