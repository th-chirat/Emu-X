/*
 * VirtualGamepad.kt
 * -----------------------------------------------------------------------------
 * จอยเสมือนบนหน้าจอ — วาดปุ่มตามผัง (ControllerLayout) แล้วแปลงการแตะเป็น input ของเกม
 *
 * ออกแบบให้ "แต่ละปุ่มเป็นกล่องแยกของตัวเอง":
 *   - กดหลายปุ่มพร้อมกันได้ (multi-touch) เพราะแต่ละนิ้วจับคนละกล่อง
 *   - แตะพื้นที่ว่าง (ไม่โดนปุ่ม) การแตะจะทะลุไปถึงจอเกม → ทัชสกรีน NDS ใช้ได้
 *
 * การส่ง input:
 *   ปุ่มทั่วไป → onKey(keyCode, กด/ปล่อย)
 *   ปุ่มทิศ    → onMotion(DPAD, x, y) โดย x,y ∈ {-1, 0, 1}
 *   อนาล็อก    → onMotion(ANALOG_LEFT/RIGHT, x, y) โดย x,y ∈ [-1, 1]
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player.gamepad

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.allemulator.core.emulation.ButtonKind
import com.allemulator.core.emulation.VirtualButton
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** แหล่งของ motion event (ภายในแอพ) — GameActivity แปลงเป็นค่าคงที่ของ LibretroDroid */
enum class MotionSource { DPAD, ANALOG_LEFT, ANALOG_RIGHT }

/** ตัวรับ input จากจอยเสมือน */
interface GamepadListener {
    fun onKey(keyCode: Int, pressed: Boolean)
    fun onMotion(source: MotionSource, x: Float, y: Float)
}

/**
 * คำนวณกรอบ (pixel) ของปุ่มบนจอขนาด w x h
 * ปุ่มไหล่/ปุ่มเล็กเป็นแคปซูลแนวนอน ปุ่มอื่นเป็นสี่เหลี่ยมจัตุรัส (วาดเป็นวงกลม)
 */
internal fun VirtualButton.bounds(w: Float, h: Float): Rect {
    val base = size * min(w, h)
    val (bw, bh) = when (kind) {
        ButtonKind.SHOULDER -> base * 1.4f to base * 0.6f
        ButtonKind.SMALL -> base * 1.9f to base * 0.55f
        else -> base to base
    }
    val cx = x * w
    val cy = y * h
    return Rect(cx - bw / 2, cy - bh / 2, cx + bw / 2, cy + bh / 2)
}

/**
 * จอยเสมือนสำหรับเล่นเกม
 *
 * @param buttons  ปุ่มทั้งหมดตามทิศจอปัจจุบัน
 * @param haptics  สั่นเบาๆ ตอนกดปุ่ม
 */
@Composable
fun VirtualGamepad(
    buttons: List<VirtualButton>,
    listener: GamepadListener,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val feedback = { if (haptics) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val w = with(density) { maxWidth.toPx() }
        val h = with(density) { maxHeight.toPx() }

        buttons.forEach { button ->
            val r = button.bounds(w, h)
            val boxModifier = Modifier
                .offset { IntOffset(r.left.roundToInt(), r.top.roundToInt()) }
                .size(with(density) { r.width.toDp() }, with(density) { r.height.toDp() })

            when (button.kind) {
                ButtonKind.DPAD -> DPadButton(button, boxModifier, listener, feedback)
                ButtonKind.ANALOG -> AnalogStick(button, boxModifier, listener, feedback)
                else -> PressButton(button, boxModifier, listener, feedback)
            }
        }
    }
}

// =============================================================================
// ปุ่มกดธรรมดา (A/B/X/Y, L/R, Start/Select)
// =============================================================================
@Composable
private fun PressButton(button: VirtualButton, modifier: Modifier, listener: GamepadListener, feedback: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier.pointerInput(button.keyCode) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false).consume()
                pressed = true
                feedback()
                listener.onKey(button.keyCode, true)
                // รอจนนิ้วยกขึ้น (หรือ gesture ถูกยกเลิก)
                do {
                    val event = awaitPointerEvent()
                    event.changes.forEach { it.consume() }
                } while (event.changes.any { it.pressed })
                pressed = false
                listener.onKey(button.keyCode, false)
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        ButtonFace(button, pressed)
    }
}

/** หน้าตาปุ่ม (ใช้ทั้งตอนเล่นและตอนแก้ไขผัง) */
@Composable
internal fun ButtonFace(button: VirtualButton, pressed: Boolean, selected: Boolean = false) {
    Canvas(Modifier.fillMaxSize()) {
        val a = button.alpha
        val fill = Color.White.copy(alpha = (if (pressed) 0.50f else 0.22f) * a)
        val edge = (if (selected) Color(0xFFFFB020) else Color.White).copy(alpha = if (selected) 1f else 0.70f * a)
        val stroke = Stroke(width = (if (selected) 3f else 1.5f) * density)
        when (button.kind) {
            ButtonKind.FACE, ButtonKind.ANALOG -> {
                drawCircle(fill)
                drawCircle(edge, style = stroke)
            }
            ButtonKind.DPAD -> drawDpad(fill, edge, stroke, pressedDir = null)
            else -> {
                val rad = CornerRadius(size.height / 2, size.height / 2)
                drawRoundRect(fill, cornerRadius = rad)
                drawRoundRect(edge, cornerRadius = rad, style = stroke)
            }
        }
    }
    if (button.label.isNotEmpty()) {
        Text(
            button.label,
            color = Color.White.copy(alpha = 0.9f * button.alpha.coerceAtLeast(0.4f)),
            fontWeight = FontWeight.Bold,
            fontSize = when (button.kind) {
                ButtonKind.SMALL -> 10.sp
                ButtonKind.SHOULDER -> 14.sp
                else -> 20.sp
            },
        )
    }
}

/** วาดปุ่มทิศรูปกากบาท ไฮไลต์ทิศที่กดอยู่ */
private fun DrawScope.drawDpad(fill: Color, edge: Color, stroke: Stroke, pressedDir: Offset?) {
    val s = size.minDimension
    val arm = s / 3f
    val cx = size.width / 2
    val cy = size.height / 2
    val rad = CornerRadius(arm * 0.25f, arm * 0.25f)
    val h = Rect(Offset(cx - s / 2, cy - arm / 2), Size(s, arm))
    val v = Rect(Offset(cx - arm / 2, cy - s / 2), Size(arm, s))
    listOf(h, v).forEach {
        drawRoundRect(fill, it.topLeft, it.size, rad)
        drawRoundRect(edge, it.topLeft, it.size, rad, style = stroke)
    }
    pressedDir?.let { d ->
        val hl = Color.White.copy(alpha = 0.5f)
        if (d.x != 0f) drawCircle(hl, arm * 0.35f, Offset(cx + d.x * s / 3, cy))
        if (d.y != 0f) drawCircle(hl, arm * 0.35f, Offset(cx, cy + d.y * s / 3))
    }
}

// =============================================================================
// ปุ่มทิศ (D-Pad) — รองรับ 8 ทิศ (รวมทแยง)
// =============================================================================
@Composable
private fun DPadButton(button: VirtualButton, modifier: Modifier, listener: GamepadListener, feedback: () -> Unit) {
    var dir by remember { mutableStateOf(Offset.Zero) }

    fun update(pos: Offset, w: Float, h: Float) {
        val dx = (pos.x - w / 2) / (w / 2)
        val dy = (pos.y - h / 2) / (h / 2)
        val dist = sqrt(dx * dx + dy * dy)
        val newDir = if (dist < 0.22f) {
            Offset.Zero // นิ้วอยู่ตรงกลาง = ไม่กดทิศไหน
        } else {
            // แบ่งวงกลมเป็น 8 ชิ้น ชิ้นละ 45°
            val angle = Math.toDegrees(atan2(dy, dx).toDouble())
            val sector = ((angle + 360 + 22.5) % 360 / 45).toInt()
            val table = arrayOf(1f to 0f, 1f to 1f, 0f to 1f, -1f to 1f, -1f to 0f, -1f to -1f, 0f to -1f, 1f to -1f)
            Offset(table[sector].first, table[sector].second)
        }
        if (newDir != dir) {
            if (newDir != Offset.Zero) feedback()
            dir = newDir
            listener.onMotion(MotionSource.DPAD, newDir.x, newDir.y)
        }
    }

    Box(
        modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                update(down.position, size.width.toFloat(), size.height.toFloat())
                do {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    change.consume()
                    if (change.pressed) update(change.position, size.width.toFloat(), size.height.toFloat())
                } while (change.pressed)
                dir = Offset.Zero
                listener.onMotion(MotionSource.DPAD, 0f, 0f)
            }
        },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val a = button.alpha
            drawDpad(
                fill = Color.White.copy(alpha = 0.16f * a),
                edge = Color.White.copy(alpha = 0.55f * a),
                stroke = Stroke(width = 1.5f * density),
                pressedDir = dir.takeIf { it != Offset.Zero },
            )
        }
    }
}

// =============================================================================
// อนาล็อกสติ๊ก — ลากนิ้วจากจุดที่แตะ ระยะลากสูงสุด = รัศมีวงกลม
// =============================================================================
@Composable
private fun AnalogStick(button: VirtualButton, modifier: Modifier, listener: GamepadListener, feedback: () -> Unit) {
    var knob by remember { mutableStateOf(Offset.Zero) } // -1..1
    val source = if (button.analogSource == 1) MotionSource.ANALOG_RIGHT else MotionSource.ANALOG_LEFT

    Box(
        modifier.pointerInput(Unit) {
            val radius = size.width / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            fun update(pos: Offset) {
                var v = (pos - center) / radius
                val len = v.getDistance()
                if (len > 1f) v /= len // ไม่ให้เกินขอบวง
                knob = v
                listener.onMotion(source, v.x, v.y)
            }
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                feedback()
                update(down.position)
                do {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    change.consume()
                    if (change.pressed) update(change.position)
                } while (change.pressed)
                knob = Offset.Zero
                listener.onMotion(source, 0f, 0f)
            }
        },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val a = button.alpha
            val r = size.minDimension / 2
            drawCircle(Color.White.copy(alpha = 0.10f * a))
            drawCircle(Color.White.copy(alpha = 0.45f * a), style = Stroke(1.5f * density))
            val knobCenter = center + Offset(knob.x * r * 0.6f, knob.y * r * 0.6f)
            drawCircle(Color.White.copy(alpha = (if (abs(knob.x) + abs(knob.y) > 0) 0.55f else 0.30f) * a), r * 0.42f, knobCenter)
        }
    }
}
