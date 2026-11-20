/*
 * Theme.kt
 * -----------------------------------------------------------------------------
 * ธีมหลักของแอพ (สี, ตัวอักษร) — แนว Liquid Glass ใช้พื้นหลังเข้ม + สีสด
 * เพื่อให้กระจกโปร่งแสงดูเด่น
 *
 * อยากเปลี่ยนสีทั้งแอพ? แก้ที่ GlassColors ที่เดียว
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState

/** สีหลักของระบบกระจก */
object GlassColors {
    val Background = Color(0xFF070A18)      // พื้นหลังสุด (เกือบดำ อมน้ำเงิน)
    val BlobA = Color(0xFF3D5AFE)           // หยดสีที่ลอยอยู่หลังกระจก
    val BlobB = Color(0xFFB13DFF)
    val BlobC = Color(0xFF00C2FF)
    val BlobD = Color(0xFFFF4D8D)

    val GlassFill = Color(0x26FFFFFF)        // เนื้อกระจก (ขาว 15%)
    val GlassFillStrong = Color(0x40FFFFFF)  // กระจกที่ทึบกว่า (ปุ่มกำลังกด)
    val GlassFallback = Color(0xCC1A1F3A)    // Android 8–11 ที่เบลอไม่ได้ → ใช้สีทึบโปร่งแทน
    val EdgeLight = Color(0x99FFFFFF)        // ขอบสะท้อนแสงด้านบน-ซ้าย
    val EdgeDark = Color(0x14FFFFFF)         // ขอบด้านล่าง-ขวา

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xB3FFFFFF)
    val Accent = Color(0xFF7C9CFF)
    val Beta = Color(0xFFFFB020)
    val Danger = Color(0xFFFF5A6E)
}

/**
 * HazeState ที่ใช้ร่วมกันทั้งหน้า: พื้นหลังเป็น "ต้นทาง" (hazeSource)
 * ส่วนการ์ด/ปุ่มกระจกเป็น "ผู้เบลอ" (hazeEffect) ที่อ่านภาพจากต้นทางนี้
 */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

private val GlassTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
)

private val GlassColorScheme = darkColorScheme(
    primary = GlassColors.Accent,
    onPrimary = Color.White,
    secondary = GlassColors.BlobB,
    background = GlassColors.Background,
    onBackground = GlassColors.TextPrimary,
    surface = GlassColors.GlassFallback,
    onSurface = GlassColors.TextPrimary,
    onSurfaceVariant = GlassColors.TextSecondary,
    error = GlassColors.Danger,
)

/** ห่อทุกหน้าจอด้วยฟังก์ชันนี้ เพื่อให้ได้สี/ตัวอักษรของแอพ */
@Composable
fun AllEmulatorTheme(hazeState: HazeState? = null, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GlassColorScheme, typography = GlassTypography) {
        CompositionLocalProvider(LocalHazeState provides hazeState, content = content)
    }
}
