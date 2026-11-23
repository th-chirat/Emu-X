/*
 * PlayerOverlays.kt
 * -----------------------------------------------------------------------------
 * ส่วนที่ลอยทับจอเกม:
 *   PerfOverlay — กล่องเล็กโชว์ FPS / CPU / RAM / อุณหภูมิ (เลือกเปิดได้ทีละอย่าง)
 *   PauseMenu   — เมนูหยุดเกม: เล่นต่อ, เซฟ/โหลด 4 ช่อง, เร่งความเร็ว, ตั้งค่า, รีเซ็ต, ออก
 *                 (ไม่มีปุ่มซ่อนจอยแล้ว — ต่อจอยจริงเมื่อไหร่ จอยบนจอซ่อนเองอัตโนมัติ)
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player

import android.graphics.Bitmap
import com.allemulator.core.emulation.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allemulator.core.designsystem.glass.GlassButton
import com.allemulator.core.designsystem.glass.SnapshotGlass
import com.allemulator.core.designsystem.glass.glass
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.emulation.OverlayCorner
import com.allemulator.core.emulation.OverlaySettings
import java.text.DateFormat
import java.util.Date

// =============================================================================
// Overlay ประสิทธิภาพ
// =============================================================================
@Composable
fun PerfOverlay(p: PerfSnapshot, settings: OverlaySettings, modifier: Modifier = Modifier) {
    val lines = buildList {
        if (settings.showFps) add("FPS  ${p.fps}")
        if (settings.showCpu) add("CPU  ${p.cpuPercent?.let { "$it%" } ?: "…"}")
        if (settings.showRam) add("RAM  ${p.ramMb?.let { "$it MB" } ?: "…"}")
        if (settings.showBatteryTemp) add("TEMP ${p.batteryTempC?.let { "%.1f°C".format(it) } ?: "…"}")
    }
    // สี FPS: เขียว = ลื่น, เหลือง = พอได้, แดง = กระตุก
    val fpsColor = when {
        p.fps >= 55 -> Color(0xFF5BE38A)
        p.fps >= 40 -> Color(0xFFFFC94D)
        else -> Color(0xFFFF5A6E)
    }
    val alignment = when (settings.corner) {
        OverlayCorner.TOP_START -> Alignment.TopStart
        OverlayCorner.TOP_END -> Alignment.TopEnd
        OverlayCorner.BOTTOM_START -> Alignment.BottomStart
        OverlayCorner.BOTTOM_END -> Alignment.BottomEnd
    }
    Box(modifier.fillMaxSize(), contentAlignment = alignment) {
        Column(
            Modifier
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            lines.forEach { line ->
                Text(
                    line,
                    color = if (line.startsWith("FPS")) fpsColor else Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

// =============================================================================
// เมนูหยุดเกม
// =============================================================================
@Composable
fun PauseMenu(
    title: String,
    snapshot: Bitmap?,
    fastForward: Boolean,
    slotInfo: (Int) -> Long?,
    onResume: () -> Unit,
    onSave: (Int) -> Unit,
    onLoad: (Int) -> Unit,
    onToggleFastForward: () -> Unit,
    onReset: () -> Unit,
    onExit: () -> Unit,
    settingsContent: @Composable () -> Unit,
) {
    // โหมดเลือกช่องเซฟ: null = เมนูหลัก, true = กำลังเซฟ, false = กำลังโหลด
    var slotMode by remember { mutableStateOf<Boolean?>(null) }
    // true = กำลังเปิดหน้า "ตั้งค่าระหว่างเล่น"
    var settingsOpen by remember { mutableStateOf(false) }

    // พื้นหลัง = ภาพเกมตอนกดหยุดแบบเบลอ → เมนูเป็นกระจกฝ้าโปร่ง (liquid glass) ไม่ใช่กล่องดำ
    SnapshotGlass(snapshot, Modifier.fillMaxSize().clickable(onClick = onResume)) {
        Column(
            Modifier
                .align(Alignment.Center)
                .widthIn(max = 420.dp)
                .padding(24.dp)
                .glass(RoundedCornerShape(28.dp))
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)

            if (settingsOpen) {
                settingsContent()
                GlassButton(tr("ย้อนกลับ", "Back"), { settingsOpen = false }, Modifier.fillMaxWidth())
            } else when (val mode = slotMode) {
                null -> {
                    GlassButton(tr("เล่นต่อ", "Continue"), onResume, Modifier.fillMaxWidth(), icon = Icons.Rounded.PlayArrow, tint = GlassColors.Accent)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassButton(tr("เซฟเกม", "Save"), { slotMode = true }, Modifier.weight(1f))
                        GlassButton(tr("โหลดเซฟ", "Load"), { slotMode = false }, Modifier.weight(1f))
                    }
                    GlassButton(if (fastForward) tr("ความเร็วปกติ", "Normal speed") else tr("เร่งความเร็ว x2", "Fast forward x2"), onToggleFastForward, Modifier.fillMaxWidth(), icon = Icons.Rounded.FastForward)
                    GlassButton(tr("ตั้งค่า", "Settings"), { settingsOpen = true }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Settings)
                    GlassButton(tr("เริ่มเกมใหม่", "Restart game"), onReset, Modifier.fillMaxWidth(), icon = Icons.Rounded.RestartAlt)
                    GlassButton(tr("ออกจากเกม", "Exit game"), onExit, Modifier.fillMaxWidth(), icon = Icons.AutoMirrored.Rounded.ExitToApp, tint = GlassColors.Danger)
                }
                else -> {
                    Text(if (mode) tr("เลือกช่องที่จะเซฟ", "Choose a slot to save") else tr("เลือกช่องที่จะโหลด", "Choose a slot to load"), color = GlassColors.TextSecondary)
                    (1..4).forEach { slot ->
                        val time = slotInfo(slot)
                        val label = tr("ช่อง $slot  —  ", "Slot $slot  —  ") + (time?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it)) } ?: tr("ว่าง", "Empty"))
                        GlassButton(
                            label,
                            onClick = { if (mode) onSave(slot) else if (time != null) onLoad(slot) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    GlassButton(tr("ย้อนกลับ", "Back"), { slotMode = null }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}
