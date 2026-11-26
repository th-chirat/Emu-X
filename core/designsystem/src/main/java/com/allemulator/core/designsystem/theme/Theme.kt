/*
 * Theme.kt
 * -----------------------------------------------------------------------------
 * ธีมหลักของแอพ (สี, ตัวอักษร) — แนว Liquid Glass มี 2 โทน: มืด / สว่าง
 * ผู้ใช้เลือกได้ในหน้าตั้งค่า: ตามระบบ / สว่าง / มืด (ThemePrefs)
 *
 * อยากเปลี่ยนสีทั้งแอพ? แก้ที่ DarkPalette / LightPalette ที่เดียว
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.allemulator.core.emulation.ThemeMode
import com.allemulator.core.emulation.ThemePrefs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.allemulator.core.emulation.L10n
import dev.chrisbanes.haze.HazeState

/**
 * ชุดสี 1 ชุด (มี 2 ชุด: มืด / สว่าง) — บรีฟรอบ 4 ข้อ 3
 * หน้าจอไม่ต้องรู้ว่าตอนนี้ชุดไหน ใช้ GlassColors.xxx ได้เลย ระบบเลือกให้เอง
 */
@Immutable
class GlassPalette(
    val isDark: Boolean,
    val background: Color,
    val blobA: Color, val blobB: Color, val blobC: Color, val blobD: Color,
    val glassFill: Color, val glassFillStrong: Color, val glassFallback: Color,
    val edgeLight: Color, val edgeDark: Color,
    val textPrimary: Color, val textSecondary: Color,
    val accent: Color, val beta: Color, val danger: Color,
)

/** โทนมืด (แบบเดิม): พื้นเกือบดำอมน้ำเงิน + หยดสีสด กระจกขาวใส */
val DarkPalette = GlassPalette(
    isDark = true,
    background = Color(0xFF070A18),
    blobA = Color(0xFF3D5AFE), blobB = Color(0xFFB13DFF), blobC = Color(0xFF00C2FF), blobD = Color(0xFFFF4D8D),
    glassFill = Color(0x26FFFFFF),        // ขาว 15%
    glassFillStrong = Color(0x40FFFFFF),  // ตอนกดปุ่ม
    glassFallback = Color(0xCC1A1F3A),    // Android 8–11 ที่เบลอไม่ได้
    edgeLight = Color(0x99FFFFFF), edgeDark = Color(0x14FFFFFF),
    textPrimary = Color(0xFFFFFFFF), textSecondary = Color(0xB3FFFFFF),
    accent = Color(0xFF7C9CFF), beta = Color(0xFFFFB020), danger = Color(0xFFFF5A6E),
)

/** โทนสว่าง: พื้นขาวอมม่วงอ่อน + หยดสีพาสเทล กระจกขาวขุ่น ตัวหนังสือสีกรมท่าเข้ม */
val LightPalette = GlassPalette(
    isDark = false,
    background = Color(0xFFF1F2FA),
    blobA = Color(0xFF7D93FF), blobB = Color(0xFFD08BFF), blobC = Color(0xFF6FD8FF), blobD = Color(0xFFFF8FB8),
    glassFill = Color(0x80FFFFFF),        // ขาว 50% (ขุ่นกว่าโหมดมืด ตัวหนังสือจะได้อ่านง่าย)
    glassFillStrong = Color(0xB3FFFFFF),
    glassFallback = Color(0xE6FFFFFF),
    edgeLight = Color(0xFFFFFFFF), edgeDark = Color(0x1A1A1F3A),
    textPrimary = Color(0xFF15182E), textSecondary = Color(0xA615182E),
    accent = Color(0xFF4A63F0), beta = Color(0xFFC77700), danger = Color(0xFFD7263D),
)

/** ชุดสีที่ใช้อยู่ตอนนี้ (AllEmulatorTheme เป็นคนใส่ค่า) */
val LocalGlassPalette = staticCompositionLocalOf { DarkPalette }

/** สีหลักของระบบกระจก — อ่านจากชุดสีปัจจุบัน (มืด/สว่าง) อัตโนมัติ */
object GlassColors {
    val isDark: Boolean @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.isDark
    val Background: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.background
    val BlobA: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.blobA
    val BlobB: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.blobB
    val BlobC: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.blobC
    val BlobD: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.blobD

    val GlassFill: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.glassFill
    val GlassFillStrong: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.glassFillStrong
    val GlassFallback: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.glassFallback
    val EdgeLight: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.edgeLight
    val EdgeDark: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.edgeDark

    val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.textPrimary
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.textSecondary
    val Accent: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.accent
    val Beta: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.beta
    val Danger: Color @Composable @ReadOnlyComposable get() = LocalGlassPalette.current.danger
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

/** สีของชิ้นส่วน Material (เช่น กล่องข้อความ AlertDialog) ให้เข้ากับชุดสี */
private fun colorSchemeOf(p: GlassPalette) = if (p.isDark) {
    darkColorScheme(
        primary = p.accent, onPrimary = Color.White, secondary = p.blobB,
        background = p.background, onBackground = p.textPrimary,
        surface = p.glassFallback, onSurface = p.textPrimary, onSurfaceVariant = p.textSecondary,
        surfaceContainerHigh = Color(0xFF1C2140), error = p.danger,
    )
} else {
    lightColorScheme(
        primary = p.accent, onPrimary = Color.White, secondary = p.blobB,
        background = p.background, onBackground = p.textPrimary,
        surface = Color(0xFFFFFFFF), onSurface = p.textPrimary, onSurfaceVariant = p.textSecondary,
        surfaceContainerHigh = Color(0xFFF7F7FD), error = p.danger,
    )
}

/**
 * ห่อทุกหน้าจอด้วยฟังก์ชันนี้ เพื่อให้ได้สี/ตัวอักษรของแอพ
 * @param forceDark true = ใช้โทนมืดเสมอ (หน้าเล่นเกม — จอเกมดูดีบนพื้นมืด)
 */
@Composable
fun AllEmulatorTheme(hazeState: HazeState? = null, forceDark: Boolean = false, content: @Composable () -> Unit) {
    // เปลี่ยนภาษา (ไทย/English) → วาดหน้าจอใหม่ทั้งหมด ข้อความ tr() จึงเปลี่ยนทันที
    val language by L10n.language.collectAsState()
    val mode by ThemePrefs.mode.collectAsState()
    val dark = forceDark || when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = if (dark) DarkPalette else LightPalette

    // ไอคอนแถบสถานะ (นาฬิกา/แบต): โหมดสว่าง → ไอคอนสีเข้ม, โหมดมืด → ไอคอนสีขาว
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalGlassPalette provides palette) {
        MaterialTheme(colorScheme = colorSchemeOf(palette), typography = GlassTypography) {
            CompositionLocalProvider(LocalHazeState provides hazeState) {
                key(language) { content() }
            }
        }
    }
}
