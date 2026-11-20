/*
 * LiquidBackground.kt
 * -----------------------------------------------------------------------------
 * พื้นหลัง "ของเหลว" — หยดสีขนาดใหญ่ 4 หยดค่อยๆ ลอยไปมาอย่างช้าๆ
 * กระจกที่วางทับจะเบลอหยดสีเหล่านี้ จึงได้ลุค Liquid Glass
 *
 * ประสิทธิภาพ: วาดด้วย Canvas + radial gradient ล้วน ไม่มีรูปภาพ
 * กินแบตน้อย และหยุดวาดเองเมื่อแอพอยู่เบื้องหลัง
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.allemulator.core.designsystem.theme.GlassColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.cos
import kotlin.math.sin

/**
 * พื้นหลังของเหลว + เนื้อหาที่วางทับ
 *
 * @param hazeState ถ้าส่งมา พื้นหลังจะเป็น "ต้นทาง" ให้กระจกเบลอ
 * @param tint      สีเสริม (เช่นสีประจำเครื่องเกม) ผสมเข้าไปเป็นหยดที่ 5
 */
@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    tint: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "liquid")
    // t วิ่ง 0 → 2π วนไปเรื่อยๆ ทุก 40 วินาที (ช้าพอให้ดูนิ่ง สบายตา)
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(40_000, easing = LinearEasing), RepeatMode.Restart),
        label = "t",
    )

    Box(modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .then(if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier),
        ) {
            drawRect(GlassColors.Background)
            blob(GlassColors.BlobA, 0.20f + 0.10f * cos(t), 0.18f + 0.08f * sin(t), 0.65f)
            blob(GlassColors.BlobB, 0.85f + 0.08f * sin(t * 2), 0.35f + 0.10f * cos(t), 0.55f)
            blob(GlassColors.BlobC, 0.30f + 0.12f * sin(t), 0.80f + 0.06f * cos(t * 2), 0.60f)
            blob(GlassColors.BlobD, 0.80f + 0.06f * cos(t), 0.90f + 0.08f * sin(t), 0.45f)
            tint?.let { blob(it, 0.55f + 0.15f * sin(t), 0.50f + 0.12f * cos(t), 0.70f) }
        }
        content()
    }
}

/** วาดหยดสี 1 หยด: (cx, cy) เป็นสัดส่วนของจอ, radius เป็นสัดส่วนของด้านยาว */
private fun DrawScope.blob(color: Color, cx: Float, cy: Float, radius: Float) {
    val r = maxOf(size.width, size.height) * radius
    val center = Offset(size.width * cx, size.height * cy)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0f)),
            center = center,
            radius = r,
        ),
        radius = r,
        center = center,
    )
}
