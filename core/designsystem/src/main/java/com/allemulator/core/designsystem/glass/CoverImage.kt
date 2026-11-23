/*
 * CoverImage.kt
 * -----------------------------------------------------------------------------
 * แสดงปกเกมที่ผู้ใช้ใส่เอง (ไฟล์ .jpg ในแอพ) ถ้าไม่มีปก → แสดง [fallback] (เช่น ไอคอนเครื่องเกม)
 * โหลดรูปบน background thread เพื่อไม่ให้รายการเกมกระตุกตอนเลื่อน
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.glass

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @param file     ไฟล์ปก (null = ไม่มีปก)
 * @param version  ตัวเลขที่เปลี่ยนเมื่อปกถูกแก้ → บังคับโหลดใหม่ (ชื่อไฟล์เดิมแต่รูปใหม่)
 */
@Composable
fun CoverImage(file: File?, version: Int, modifier: Modifier = Modifier, fallback: @Composable () -> Unit) {
    val bitmap by produceState<ImageBitmap?>(null, file?.path, version) {
        value = file?.let { f ->
            withContext(Dispatchers.IO) { runCatching { BitmapFactory.decodeFile(f.path)?.asImageBitmap() }.getOrNull() }
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        val img = bitmap
        if (img != null) {
            Image(img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)))
        } else {
            fallback()
        }
    }
}
