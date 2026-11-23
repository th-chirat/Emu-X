/*
 * CoverStore.kt
 * -----------------------------------------------------------------------------
 * ปกเกมที่ผู้ใช้ใส่เอง (บรีฟรอบ 2 ข้อ 7): กดค้างที่เกม → "เปลี่ยนปกเกม" → เลือกรูป
 *
 * เก็บยังไง:
 *   - ก๊อปรูปที่เลือกมาไว้ในแอพ (files/covers/<รหัสเกม>.jpg) ย่อเหลือด้านยาวไม่เกิน 512 px
 *     → ไม่เปลืองที่ และรูปไม่หายแม้ผู้ใช้ลบรูปต้นฉบับในแกลเลอรี
 *   - ไม่ต้องแก้ฐานข้อมูล: ชื่อไฟล์ปกคำนวณจาก URI ของเกม (SHA-1)
 *   - [version] เพิ่มขึ้นทุกครั้งที่ปกเปลี่ยน → หน้าจอรู้ว่าต้องโหลดรูปใหม่
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir get() = File(context.filesDir, "covers").apply { mkdirs() }

    private val _version = MutableStateFlow(0)

    /** เปลี่ยนทุกครั้งที่มีการใส่/ลบปก */
    val version: StateFlow<Int> = _version

    /** ไฟล์ปกของเกมนี้ (null = ยังไม่มีปก) */
    fun coverFile(gameUri: String): File? = fileFor(gameUri).takeIf { it.exists() }

    /** ใส่ปกจากรูปที่ผู้ใช้เลือก (content:// จาก Photo Picker) — ย่อรูปก่อนเก็บ */
    suspend fun setCover(gameUri: String, image: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            // อ่านขนาดก่อน แล้วค่อยถอดรูปแบบย่อ (กันรูปใหญ่มากทำหน่วยความจำเต็ม)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(image)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIZE) sample *= 2
            val decoded = resolver.openInputStream(image)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching false
            val scale = MAX_SIZE.toFloat() / maxOf(decoded.width, decoded.height)
            val bitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt(), (decoded.height * scale).toInt(), true)
            } else decoded
            fileFor(gameUri).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
            _version.value++
            true
        }.getOrDefault(false)
    }

    /** ลบปก (กลับไปใช้ไอคอนเครื่องเกม) */
    fun removeCover(gameUri: String) {
        fileFor(gameUri).delete()
        _version.value++
    }

    private fun fileFor(gameUri: String): File {
        val key = MessageDigest.getInstance("SHA-1").digest(gameUri.toByteArray())
            .joinToString("") { "%02x".format(it) }.take(16)
        return File(dir, "$key.jpg")
    }

    private companion object {
        const val MAX_SIZE = 512
    }
}
