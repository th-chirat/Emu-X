/*
 * ThemeMode.kt
 * -----------------------------------------------------------------------------
 * โหมดสีของแอพ (บรีฟรอบ 4 ข้อ 3): ตามระบบ / สว่าง / มืด — เลือกได้ในหน้าตั้งค่า
 *
 * เก็บค่าปัจจุบันไว้ใน [ThemePrefs] (เหมือน L10n ของภาษา) → ธีมเปลี่ยนทันทีทั้งแอพ
 * ค่าที่ผู้ใช้เลือกถูกบันทึกใน SettingsRepository และโหลดตอนเปิดแอพ (AllEmulatorApp)
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode(val code: String, private val th: String, private val en: String) {
    SYSTEM("system", "ตามระบบ", "Follow system"),
    LIGHT("light", "สว่าง", "Light"),
    DARK("dark", "มืด", "Dark");

    val title get() = tr(th, en)

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code }
    }
}

/** โหมดสีที่ใช้อยู่ตอนนี้ (ค่าเริ่มต้น = ตามระบบ) */
object ThemePrefs {
    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode
    fun set(mode: ThemeMode) { _mode.value = mode }
}
