/*
 * LayoutEditor.kt
 * -----------------------------------------------------------------------------
 * หน้าแก้ไขผังปุ่ม (บรีฟข้อ 1.2.1 "ปรับขนาด เคลื่อนย้ายตำแหน่งปุ่ม")
 *
 *   - ลากปุ่มเพื่อย้ายตำแหน่ง
 *   - แตะปุ่มเพื่อเลือก แล้วใช้แถบเลื่อนด้านล่างปรับ "ขนาด" และ "ความโปร่งใส"
 *   - หมุนมือถือเพื่อแก้ผังแนวนอน/แนวตั้ง (เก็บแยกกัน)
 *   - ปุ่ม "รีเซ็ต" กลับค่าเริ่มต้น / "บันทึก" เก็บผังนี้
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player.gamepad

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import com.allemulator.core.emulation.tr
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.allemulator.core.data.SettingsRepository
import com.allemulator.core.designsystem.glass.GlassButton
import com.allemulator.core.designsystem.glass.GlassIconButton
import com.allemulator.core.designsystem.glass.glass
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.emulation.ControllerLayout
import com.allemulator.core.emulation.ControllerLayouts
import com.allemulator.core.emulation.GameSystem
import com.allemulator.core.emulation.VirtualButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

// =============================================================================
// ViewModel: ถือผังปุ่มที่กำลังแก้ (ยังไม่บันทึกจนกว่าจะกด "บันทึก")
// =============================================================================
@HiltViewModel
class LayoutEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settings: SettingsRepository,
) : ViewModel() {

    val system: GameSystem = GameSystem.fromId(checkNotNull(savedStateHandle.get<String>("systemId"))) ?: GameSystem.NES

    /** ผังที่กำลังแก้ (null = กำลังโหลด) */
    val layout = MutableStateFlow<ControllerLayout?>(null)

    init {
        viewModelScope.launch { layout.value = settings.layout(system).first() }
    }

    /** แก้ปุ่ม 1 ปุ่ม ในผังของทิศจอที่ระบุ */
    fun update(landscape: Boolean, id: String, change: (VirtualButton) -> VirtualButton) {
        val current = layout.value ?: return
        fun List<VirtualButton>.patch() = map { if (it.id == id) change(it) else it }
        layout.value = if (landscape) current.copy(landscape = current.landscape.patch())
        else current.copy(portrait = current.portrait.patch())
    }

    fun reset() { layout.value = ControllerLayouts.defaultFor(system) }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        layout.value?.let { settings.setLayout(system, it) }
        onDone()
    }
}

// =============================================================================
// หน้าจอ
// =============================================================================
@Composable
fun LayoutEditorScreen(onBack: () -> Unit, viewModel: LayoutEditorViewModel = hiltViewModel()) {
    val layout by viewModel.layout.collectAsStateWithLifecycle()
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var selectedId by remember { mutableStateOf<String?>(null) }
    val buttons = layout?.forOrientation(landscape) ?: return
    val selected = buttons.firstOrNull { it.id == selectedId }

    // ซ่อนแถบสถานะ/แถบนำทางเหมือนตอนเล่นเกม → ตำแหน่งปุ่มตรงกับในเกมเป๊ะ และปุ่มไม่จมใต้แถบสถานะ
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF05070F))) {
        // พื้นที่จำลอง "จอเกม" ให้กะตำแหน่งปุ่มได้
        Box(
            Modifier
                .then(if (landscape) Modifier.fillMaxHeight().fillMaxWidth(0.62f).align(Alignment.Center) else Modifier.fillMaxWidth().fillMaxHeight(0.5f))
                .background(Color(0xFF151B33)),
            contentAlignment = Alignment.Center,
        ) {
            Text(tr("พื้นที่จอเกม\n(หมุนมือถือเพื่อแก้ผังอีกแนว)", "Game screen area\n(rotate the phone to edit the other layout)"), color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }

        // ปุ่มที่ลากได้
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val density = LocalDensity.current
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            buttons.forEach { b ->
                val r = b.bounds(w, h)
                Box(
                    Modifier
                        .offset { IntOffset(r.left.roundToInt(), r.top.roundToInt()) }
                        .size(with(density) { r.width.toDp() }, with(density) { r.height.toDp() })
                        .pointerInput(b.id) { detectTapGestures { selectedId = b.id } }
                        .pointerInput(b.id, w, h) {
                            detectDragGestures(onDragStart = { selectedId = b.id }) { change, drag ->
                                change.consume()
                                viewModel.update(landscape, b.id) {
                                    it.copy(
                                        x = (it.x + drag.x / w).coerceIn(0.02f, 0.98f),
                                        y = (it.y + drag.y / h).coerceIn(0.02f, 0.98f),
                                    )
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    ButtonFace(b.copy(alpha = b.alpha.coerceAtLeast(0.5f)), pressed = false, selected = b.id == selectedId)
                }
            }
        }

        // แถบบน: กลับ / รีเซ็ต / บันทึก
        // แนวนอน: อยู่ในช่วงกลาง (เหนือพื้นที่จอเกม) ไม่ทับปุ่ม L/R ที่อยู่ริมซ้าย-ขวา
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .then(if (landscape) Modifier.fillMaxWidth(0.62f) else Modifier.fillMaxWidth())
                .safeDrawingPadding()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, tr("กลับ", "Back"), onBack)
            Text(tr("แก้ไขปุ่ม ${viewModel.system.displayName}", "Edit ${viewModel.system.displayName} buttons"), color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            GlassButton(tr("รีเซ็ต", "Reset"), { viewModel.reset() }, icon = Icons.Rounded.RestartAlt)
            GlassButton(tr("บันทึก", "Save"), { viewModel.save(onBack) }, icon = Icons.Rounded.Save, tint = GlassColors.Accent)
        }

        // แผงปรับขนาด/ความโปร่งใสของปุ่มที่เลือก
        if (selected != null) {
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 360.dp)
                    .safeDrawingPadding()
                    .padding(top = 72.dp, start = 16.dp, end = 16.dp)
                    .glass(RoundedCornerShape(24.dp))
                    .padding(16.dp),
            ) {
                Text(tr("ปุ่ม: ${selected.label.ifEmpty { selected.id.uppercase() }}", "Button: ${selected.label.ifEmpty { selected.id.uppercase() }}"), color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(tr("ขนาด", "Size"), color = GlassColors.TextSecondary)
                Slider(
                    value = selected.size,
                    onValueChange = { v -> viewModel.update(landscape, selected.id) { it.copy(size = v) } },
                    valueRange = 0.06f..0.5f,
                )
                Text(tr("ความทึบ", "Opacity"), color = GlassColors.TextSecondary)
                Slider(
                    value = selected.alpha,
                    onValueChange = { v -> viewModel.update(landscape, selected.id) { it.copy(alpha = v) } },
                    valueRange = 0.1f..1f,
                )
                GlassButton(tr("เสร็จ", "Done"), { selectedId = null }, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}
