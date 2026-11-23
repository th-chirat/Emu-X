/*
 * L10n.kt
 * -----------------------------------------------------------------------------
 * ระบบ 2 ภาษา (ไทย / English) — สลับได้จากสวิตช์ในหน้าแรก
 *
 * วิธีใช้ในโค้ด:  Text(tr("เล่นต่อ", "Continue"))
 *   tr() คืนข้อความตามภาษาที่เลือกอยู่ตอนนี้
 *
 * ทำไมไม่ใช้ strings.xml?
 *   ข้อความอยู่คู่กับโค้ดที่ใช้ อ่าน/แก้ง่าย และสลับภาษาได้ทันทีโดยไม่ต้องรีสตาร์ทแอพ
 *   (AllEmulatorTheme คอยวาดหน้าจอใหม่ทั้งหมดเมื่อภาษาเปลี่ยน)
 *
 * ค่าภาษาถูกบันทึกใน SettingsRepository (key "language") และโหลดตอนเปิดแอพ
 * อยู่ในโมดูล core:emulation เพราะเป็นโมดูลล่างสุดที่ทุกส่วนของแอพใช้ได้
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

enum class AppLanguage(val code: String) {
    TH("th"),
    EN("en"),
    ;

    companion object {
        fun fromCode(code: String?): AppLanguage? = entries.firstOrNull { it.code == code }

        /** ค่าเริ่มต้นตามภาษาของเครื่อง: เครื่องภาษาไทย → ไทย, อื่นๆ → English */
        fun systemDefault(): AppLanguage = if (Locale.getDefault().language == "th") TH else EN
    }
}

object L10n {
    private val _language = MutableStateFlow(AppLanguage.systemDefault())

    /** ภาษาปัจจุบัน (หน้าจอ Compose ฟังค่านี้เพื่อวาดใหม่เมื่อเปลี่ยน) */
    val language: StateFlow<AppLanguage> = _language

    val isThai: Boolean get() = _language.value == AppLanguage.TH

    fun set(lang: AppLanguage) { _language.value = lang }
}

/** เลือกข้อความตามภาษาที่ใช้อยู่ */
fun tr(th: String, en: String): String = if (L10n.isThai) th else en
