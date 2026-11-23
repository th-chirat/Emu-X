/*
 * SnapshotGlass.kt
 * -----------------------------------------------------------------------------
 * ทำให้ "กระจก" ใช้ได้บนหน้าเล่นเกม (บรีฟรอบ 3 ข้อ 2)
 *
 * ปัญหา: จอเกมเป็น OpenGL (SurfaceView) → Haze เบลอภาพเกมโดยตรงไม่ได้
 *         ปุ่ม/การ์ดกระจกเลยกลายเป็นสีทึบเข้มๆ
 * วิธีแก้: ตอนเปิดเมนู ถ่ายภาพหน้าจอเกมไว้ 1 ภาพ (เกมหยุดอยู่แล้ว ภาพไม่ขยับ)
 *          → ใช้ภาพนั้นเป็นพื้นหลังเบลอ + เป็น "ต้นทาง" ให้กระจกทุกชิ้นในเมนูเบลอตาม
 *          ได้ลุคกระจกฝ้าโปร่งๆ เหมือนหน้าแรกของแอพ
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.glass

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.allemulator.core.designsystem.theme.LocalHazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * @param snapshot ภาพหน้าจอตอนเปิดเมนู (null = ถ่ายไม่ได้ → ใช้พื้นมืดโปร่งแทน)
 * @param content  เนื้อหาที่ใช้ [glass] ข้างใน จะเบลอภาพ [snapshot] ให้อัตโนมัติ
 */
@Composable
fun SnapshotGlass(snapshot: Bitmap?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val hazeState = rememberHazeState()
    // ย่อภาพลงมากๆ แล้วขยายกลับ = เบลอแบบถูกๆ ที่ใช้ได้ทุกรุ่น (Android 8+)
    val blurred = remember(snapshot) {
        snapshot?.let {
            val w = (it.width / 10).coerceAtLeast(1)
            val h = (it.height / 10).coerceAtLeast(1)
            Bitmap.createScaledBitmap(it, w, h, true).asImageBitmap()
        }
    }
    Box(modifier) {
        Box(Modifier.matchParentSize().hazeSource(hazeState)) {
            if (blurred != null) {
                Image(
                    blurred,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    filterQuality = FilterQuality.High,
                    // Android 12+ เบลอซ้ำอีกชั้นให้เนียน (รุ่นเก่า Modifier.blur ไม่มีผล ก็ยังเบลอจากการย่อภาพ)
                    modifier = Modifier
                        .matchParentSize()
                        .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(12.dp) else Modifier),
                )
            }
            // ทับมืดนิดหน่อยให้ตัวหนังสือสีขาวอ่านง่าย (ไม่ดำทึบเหมือนเดิม)
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = if (blurred != null) 0.18f else 0.55f)))
        }
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            content()
        }
    }
}
