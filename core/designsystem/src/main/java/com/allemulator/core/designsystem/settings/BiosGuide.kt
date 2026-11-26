/*
 * BiosGuide.kt
 * -----------------------------------------------------------------------------
 * หน้าต่าง "วิธีติดตั้ง BIOS" (บรีฟรอบ 5 ข้อ 2) — ใช้ในหน้ารายการเกม PS2 และหน้าตั้งค่า PS2
 *
 * ข้อห้ามตามนโยบาย Play: แอพไม่แถม BIOS และไม่บอกให้ไปโหลดจากเว็บ
 * → สอนให้ใช้ BIOS ที่ดัมพ์จากเครื่องของผู้ใช้เองเท่านั้น
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allemulator.core.emulation.GameSystem
import com.allemulator.core.emulation.tr

/**
 * @param onImport  null = ไม่โชว์ปุ่มนำเข้า (เช่นเปิดจากหน้าที่ไม่มีตัวเลือกไฟล์)
 */
@Composable
fun BiosGuideDialog(system: GameSystem, onDismiss: () -> Unit, onImport: (() -> Unit)? = null) {
    val name = system.displayName
    val steps = listOf(
        tr(
            "ดัมพ์ (คัดลอก) BIOS จากเครื่อง $name ของคุณเอง ด้วยโปรแกรมดัมพ์ BIOS ที่รันบนเครื่อง $name แล้วเซฟลง USB หรือเมมโมรี่การ์ด",
            "Dump the BIOS from your own $name with a BIOS dumper program that runs on the $name, saving it to a USB drive or memory card.",
        ),
        tr(
            "ย้ายไฟล์ BIOS มาไว้ในมือถือ (เช่น โฟลเดอร์ Download) — ไฟล์ .bin ขนาดประมาณ 4 MB หรือ .zip ที่มีไฟล์ BIOS อยู่ข้างในก็ได้",
            "Copy the BIOS file to your phone (e.g. the Download folder) — a ~4 MB .bin file, or a .zip that contains it.",
        ),
        tr(
            "ในแอพ: หน้า $name → ปุ่มเฟือง (ตั้งค่า $name) → หัวข้อ BIOS → \"นำเข้าไฟล์ BIOS\" แล้วเลือกไฟล์",
            "In the app: $name page → gear button ($name settings) → BIOS → \"Import BIOS file\", then pick the file.",
        ),
        tr(
            "ขึ้นข้อความ \"นำเข้าแล้ว\" = เสร็จ เปิดเกมได้เลย (มีหลายไฟล์ไม่เป็นไร แอพเลือกตัวที่ใช้ได้ให้เอง)",
            "When it says \"Imported\", you're done — start a game. (Several BIOS files are fine; the app picks a working one.)",
        ),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("วิธีติดตั้ง BIOS $name", "How to install the $name BIOS")) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                steps.forEachIndexed { i, step ->
                    Row {
                        Text("${i + 1}.", fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    tr(
                        "แอพนี้ไม่มี BIOS แถมมา และ BIOS เป็นลิขสิทธิ์ของผู้ผลิตเครื่อง — ใช้เฉพาะ BIOS จากเครื่องที่คุณเป็นเจ้าของเท่านั้น",
                        "This app doesn't include a BIOS, and the BIOS is copyrighted by the console maker — only use one from a console you own.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            if (onImport != null) {
                TextButton(onClick = { onDismiss(); onImport() }) { Text(tr("นำเข้าไฟล์ BIOS", "Import BIOS file")) }
            } else {
                TextButton(onClick = onDismiss) { Text(tr("เข้าใจแล้ว", "Got it")) }
            }
        },
        dismissButton = if (onImport != null) {
            { TextButton(onClick = onDismiss) { Text(tr("ปิด", "Close")) } }
        } else {
            null
        },
    )
}
