/*
 * AllEmulatorApp.kt
 * -----------------------------------------------------------------------------
 * คลาส Application = สิ่งแรกที่ถูกสร้างเมื่อแอพเปิด
 * @HiltAndroidApp เปิดระบบ Dependency Injection (Hilt) ให้ทั้งแอพ
 * -----------------------------------------------------------------------------
 */
package com.allemulator.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AllEmulatorApp : Application()
