/*
 * ConsoleIcon.kt
 * -----------------------------------------------------------------------------
 * ไอคอนเครื่องเกมแต่ละเครื่อง
 *   - เครื่องที่มีรูปภาพ (res/drawable-nodpi/console_*.png) → ใช้รูปภาพ
 *     รูปมาจากโฟลเดอร์ Asset_icon ของเจ้าของแอพ ตัดพื้นหลังและลบโลโก้ยี่ห้อออกแล้ว
 *     (สร้างใหม่ได้ด้วย scripts/make-console-icons.py)
 *   - ภาพวาด Canvas ด้านล่างเก็บไว้เป็นแบบสำรอง (เผื่อเพิ่มเครื่องใหม่ที่ยังไม่มีรูป)
 *
 * สำคัญ: ห้ามใช้โลโก้จริงของ Sony/Nintendo เพราะ Google Play จะปฏิเสธแอพ
 * (ละเมิดเครื่องหมายการค้า) ไอคอนนี้จึงเป็น "รูปทรงทั่วไป" ของเครื่องเล่นเกมแต่ละแบบ
 *
 * ระบบพิกัด: วาดบนกรอบ 100 x 100 หน่วย แล้วย่อ/ขยายตามขนาดจริงอัตโนมัติ
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.allemulator.core.designsystem.R
import com.allemulator.core.emulation.GameSystem

/** วาดไอคอนเครื่องเกม */
@Composable
fun ConsoleIcon(system: GameSystem, modifier: Modifier = Modifier) {
    val image = when (system) {
        GameSystem.PSP -> R.drawable.console_psp
        GameSystem.PS1 -> R.drawable.console_ps1
        GameSystem.PS2 -> R.drawable.console_ps2
        GameSystem.GB -> R.drawable.console_gb
        GameSystem.GBC -> R.drawable.console_gbc
        GameSystem.NES -> R.drawable.console_nes
        GameSystem.GBA -> R.drawable.console_gba
    }
    if (image != null) {
        Image(painterResource(image), contentDescription = system.displayName, modifier = modifier, contentScale = ContentScale.Fit)
        return
    }
    val start = Color(system.accentStart)
    val end = Color(system.accentEnd)
    Canvas(modifier) {
        val u = minOf(size.width, size.height) / 100f // 1 หน่วย = กี่ pixel
        val ox = (size.width - 100 * u) / 2            // จัดให้อยู่กึ่งกลาง
        val oy = (size.height - 100 * u) / 2
        val p = Painter(this, u, ox, oy, start, end)
        when (system) {
            GameSystem.PSP -> p.psp()
            GameSystem.PS1, GameSystem.PS2 -> p.discConsole()
            GameSystem.GB -> p.handheld(color = false)
            GameSystem.GBC -> p.handheld(color = true)
            GameSystem.NES -> p.boxConsole()
            GameSystem.GBA -> p.wideHandheld()
        }
    }
}

/** ตัวช่วยวาด: แปลงพิกัด 0..100 เป็น pixel จริง */
private class Painter(
    val d: DrawScope,
    val u: Float,
    val ox: Float,
    val oy: Float,
    val start: Color,
    val end: Color,
) {
    private val body get() = Brush.linearGradient(listOf(start, end), Offset(ox, oy), Offset(ox + 100 * u, oy + 100 * u))
    private val white = Color.White.copy(alpha = 0.92f)
    private val screen = Color(0xFF0B1022)

    private fun o(x: Float, y: Float) = Offset(ox + x * u, oy + y * u)
    private fun s(w: Float, h: Float) = Size(w * u, h * u)
    private fun r(v: Float) = CornerRadius(v * u, v * u)

    fun rect(x: Float, y: Float, w: Float, h: Float, rad: Float, brush: Brush) =
        d.drawRoundRect(brush, o(x, y), s(w, h), r(rad))

    fun rect(x: Float, y: Float, w: Float, h: Float, rad: Float, color: Color) =
        d.drawRoundRect(color, o(x, y), s(w, h), r(rad))

    fun circle(x: Float, y: Float, radius: Float, color: Color) = d.drawCircle(color, radius * u, o(x, y))

    /** ปุ่มทิศรูปกากบาท */
    fun cross(cx: Float, cy: Float, len: Float, thick: Float) {
        rect(cx - len / 2, cy - thick / 2, len, thick, 1.5f, white)
        rect(cx - thick / 2, cy - len / 2, thick, len, 1.5f, white)
    }

    // ---- เครื่องพกพาแนวนอน จอกว้าง (PSP) ----
    fun psp() {
        rect(4f, 28f, 92f, 44f, 22f, body)
        rect(26f, 34f, 48f, 32f, 3f, screen)
        cross(14f, 50f, 12f, 4f)
        circle(86f, 44f, 2.6f, white); circle(86f, 56f, 2.6f, white)
        circle(80f, 50f, 2.6f, white); circle(92f, 50f, 2.6f, white)
    }

    // ---- เครื่องพกพาแนวนอน ปุ่ม 2 ปุ่ม + ไหล่ L/R (GBA) ----
    fun wideHandheld() {
        rect(10f, 24f, 80f, 6f, 3f, white.copy(alpha = 0.55f)) // ปุ่มไหล่
        rect(6f, 28f, 88f, 46f, 18f, body)
        rect(30f, 34f, 40f, 30f, 3f, screen)
        cross(17f, 48f, 13f, 4.2f)
        circle(80f, 46f, 3.6f, white); circle(87f, 41f, 3.6f, white)
        circle(80f, 64f, 1.6f, white); circle(86f, 64f, 1.6f, white)
    }

    // ---- เครื่องคอนโซลแผ่นดิสก์ ฝาบนกลม (PS1) ----
    fun discConsole() {
        rect(8f, 30f, 84f, 44f, 6f, body)
        d.drawCircle(white, 15f * u, o(40f, 52f), style = Stroke(width = 3f * u))
        circle(40f, 52f, 3f, white)
        rect(66f, 62f, 18f, 4f, 2f, white)
        circle(70f, 40f, 2.5f, white)
    }

    // ---- เครื่องพกพาแนวตั้ง (GB / GBC) ----
    fun handheld(color: Boolean) {
        rect(22f, 6f, 56f, 88f, 8f, body)
        rect(28f, 12f, 44f, 36f, 4f, screen)
        if (color) {
            // จอสีรุ้งเล็กๆ บอกว่าเป็นรุ่นจอสี
            val rainbow = listOf(Color(0xFFFF5A6E), Color(0xFFFFC94D), Color(0xFF5BE38A), Color(0xFF5AB4FF))
            rainbow.forEachIndexed { i, c -> rect(33f + i * 9f, 40f, 7f, 3f, 1f, c) }
        } else {
            rect(32f, 16f, 36f, 26f, 2f, Color(0xFF9BBC0F).copy(alpha = 0.85f))
        }
        cross(36f, 64f, 14f, 4.6f)
        circle(62f, 66f, 4.2f, white); circle(70f, 60f, 4.2f, white)
        rect(40f, 82f, 8f, 2.6f, 1.3f, white); rect(52f, 82f, 8f, 2.6f, 1.3f, white)
    }

    // ---- เครื่องคอนโซลกล่องแบน (NES) ----
    fun boxConsole() {
        rect(8f, 32f, 84f, 40f, 4f, body)
        rect(20f, 38f, 60f, 10f, 2f, screen.copy(alpha = 0.75f))
        rect(14f, 58f, 22f, 6f, 2f, white)
        rect(44f, 58f, 10f, 6f, 2f, white); rect(58f, 58f, 10f, 6f, 2f, white)
        circle(80f, 61f, 3f, Color(0xFFFF5A6E))
    }

}
