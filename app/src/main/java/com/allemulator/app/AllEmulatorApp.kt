/*
 * AllEmulatorApp.kt
 * -----------------------------------------------------------------------------
 * คลาส Application = สิ่งแรกที่ถูกสร้างเมื่อแอพเปิด
 * @HiltAndroidApp เปิดระบบ Dependency Injection (Hilt) ให้ทั้งแอพ
 *
 * โหลดภาษาที่ผู้ใช้เลือกไว้ (ไทย/English) ก่อนหน้าจอแรกจะขึ้น แล้วคอยฟังการเปลี่ยนภาษา
 * -----------------------------------------------------------------------------
 */
package com.allemulator.app

import android.app.Application
import com.allemulator.core.data.SettingsRepository
import com.allemulator.core.emulation.L10n
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class AllEmulatorApp : Application() {

    @Inject lateinit var settings: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // อ่านค่าภาษาครั้งแรกแบบรอ (เร็วมาก ไม่กี่ ms) เพื่อไม่ให้หน้าจอแรกกระพริบเปลี่ยนภาษา
        runBlocking { settings.language.first() }?.let(L10n::set)
        appScope.launch { settings.language.filterNotNull().collect(L10n::set) }
    }
}
