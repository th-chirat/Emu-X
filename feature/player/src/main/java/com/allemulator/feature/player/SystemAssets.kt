/*
 * SystemAssets.kt
 * -----------------------------------------------------------------------------
 * core บางตัวต้องมี "ไฟล์ระบบ" อยู่ในโฟลเดอร์ system ก่อนเริ่มเกม
 * เช่น PPSSPP ต้องการฟอนต์และไฟล์ flash0 ในโฟลเดอร์ system/PPSSPP
 *
 * ไฟล์เหล่านี้ถูกแพ็กมากับแอพใน assets/system/ (ดาวน์โหลดโดย scripts/fetch-cores.sh)
 * ฟังก์ชันนี้ก๊อปออกมาไว้ในเครื่อง "ครั้งเดียว" ต่อเวอร์ชันแอพ
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import java.io.File

object SystemAssets {

    fun ensureInstalled(context: Context, systemDir: File) {
        val marker = File(systemDir, ".assets_version")
        val version = appVersion(context)
        if (marker.exists() && marker.readText() == version) return // ติดตั้งแล้ว

        copyAssetDir(context, "system", systemDir)
        marker.writeText(version)
    }

    /** ก๊อปโฟลเดอร์ใน assets แบบเรียกตัวเองซ้ำ (recursive) */
    private fun copyAssetDir(context: Context, assetPath: String, target: File) {
        val children = context.assets.list(assetPath) ?: return
        if (children.isEmpty()) {
            // ไม่มีลูก = เป็นไฟล์
            runCatching {
                context.assets.open(assetPath).use { input ->
                    target.parentFile?.mkdirs()
                    target.outputStream().use { input.copyTo(it) }
                }
            }
            return
        }
        target.mkdirs()
        children.forEach { copyAssetDir(context, "$assetPath/$it", File(target, it)) }
    }

    @Suppress("DEPRECATION")
    private fun appVersion(context: Context): String {
        val info: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val code = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
        return code.toString()
    }
}
