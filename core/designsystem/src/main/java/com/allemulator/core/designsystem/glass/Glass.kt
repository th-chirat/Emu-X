/*
 * Glass.kt
 * -----------------------------------------------------------------------------
 * หัวใจของสไตล์ Liquid Glass: Modifier.glass()
 *
 * กระจก 1 ชิ้นประกอบด้วย 3 ชั้น:
 *   1) เบลอสิ่งที่อยู่ข้างหลัง (Haze)            → ความรู้สึก "ฝ้า"
 *   2) เคลือบสีขาวโปร่งไล่ระดับ                     → เนื้อกระจก
 *   3) ขอบไล่สี สว่างบนซ้าย → จางล่างขวา            → แสงสะท้อนที่ขอบ (specular)
 *
 * Android 12+ (API 31) เบลอได้จริง
 * Android 8–11 เบลอไม่ได้ → Haze ใช้ fallbackTint (สีทึบโปร่ง) แทนอัตโนมัติ
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.designsystem.theme.LocalHazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * ทำให้ composable ใดๆ กลายเป็นกระจก
 *
 * @param shape     รูปทรง (เช่น RoundedCornerShape(24.dp))
 * @param tint      สีอมของกระจก (null = ขาวใส)
 * @param blur      ความเบลอ
 * @param pressed   true = กำลังถูกกด → กระจกทึบขึ้นเล็กน้อย
 */
fun Modifier.glass(
    shape: Shape,
    tint: Color? = null,
    blur: Dp = 24.dp,
    pressed: Boolean = false,
): Modifier = composed {
    val hazeState = LocalHazeState.current
    val fill = if (pressed) GlassColors.GlassFillStrong else GlassColors.GlassFill
    val tintColor = tint?.copy(alpha = if (pressed) 0.35f else 0.22f)

    this
        .clip(shape)
        .then(
            if (hazeState != null) {
                Modifier.hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = GlassColors.Background,
                        tints = listOfNotNull(HazeTint(fill), tintColor?.let { HazeTint(it) }),
                        blurRadius = blur,
                        noiseFactor = 0.05f,
                        fallbackTint = HazeTint(GlassColors.GlassFallback),
                    ),
                )
            } else {
                // ไม่มีต้นทางให้เบลอ (เช่นปุ่มบนจอเกม) → กระจกใสอมขาว + เงามืดบางๆ ให้ตัวหนังสืออ่านออก
                // (เดิมใช้สีทึบเข้ม ดูเป็นกล่องดำ — บรีฟรอบ 3 ข้อ 2)
                Modifier
                    .background(Color.Black.copy(alpha = 0.22f))
                    .background(fill)
            },
        )
        .background(
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.10f), Color.Transparent, (tint ?: Color.White).copy(alpha = 0.06f)),
            ),
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(listOf(GlassColors.EdgeLight, GlassColors.EdgeDark, GlassColors.EdgeDark.copy(alpha = 0.3f))),
            shape = shape,
        )
}
