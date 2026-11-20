/*
 * GameScanner.kt
 * -----------------------------------------------------------------------------
 * สแกนโฟลเดอร์ที่ผู้ใช้เลือก (ผ่าน SAF) เพื่อหาไฟล์เกม
 *
 * ทำไมไม่ใช้ DocumentFile.listFiles()?
 *   DocumentFile ถามชื่อไฟล์ทีละไฟล์ (1 query ต่อไฟล์) ช้ามากถ้ามีเกมเป็นร้อย
 *   ที่นี่จึง query ContentResolver ครั้งเดียวต่อโฟลเดอร์ ได้ชื่อ/ชนิด/ขนาดมาพร้อมกัน
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.data.scan

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.emulation.GameSystem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** ลงลึกได้ไม่เกินกี่ชั้น (กันโฟลเดอร์วนลูป/ใหญ่เกินไป) */
    private val maxDepth = 6

    /** ไฟล์ 1 ไฟล์ที่เจอระหว่างสแกน */
    private data class Child(val docId: String, val name: String, val isDir: Boolean, val size: Long)

    /**
     * สแกนโฟลเดอร์ [treeUri] หาเกมของเครื่อง [system]
     * ทำงานบน background thread (Dispatchers.IO) จึงเรียกจาก UI ได้ปลอดภัย
     */
    suspend fun scan(system: GameSystem, treeUri: Uri): List<GameEntity> = withContext(Dispatchers.IO) {
        val rootId = DocumentsContract.getTreeDocumentId(treeUri)
        val result = mutableListOf<GameEntity>()
        walk(system, treeUri, rootId, depth = 0, out = result)
        result
    }

    private fun walk(system: GameSystem, treeUri: Uri, parentId: String, depth: Int, out: MutableList<GameEntity>) {
        if (depth > maxDepth) return
        val children = listChildren(treeUri, parentId)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentId).toString()

        // กฎพิเศษของเกมแผ่น: ถ้าในโฟลเดอร์มี .cue หรือ .m3u ให้โชว์แค่ไฟล์นั้น
        // ไม่โชว์ .bin/.img ที่เป็น "ไส้ใน" ซ้ำอีก (ไม่งั้นเกมเดียวจะขึ้น 2-3 รายการ)
        val names = children.filter { !it.isDir }.map { it.name.lowercase() }
        val hasPlaylist = names.any { it.endsWith(".cue") || it.endsWith(".m3u") }

        for (child in children) {
            if (child.isDir) {
                walk(system, treeUri, child.docId, depth + 1, out)
                continue
            }
            if (!system.accepts(child.name)) continue
            val ext = child.name.substringAfterLast('.').lowercase()
            if (hasPlaylist && ext in setOf("bin", "img")) continue

            out += GameEntity(
                uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, child.docId).toString(),
                systemId = system.id,
                title = cleanTitle(child.name),
                fileName = child.name,
                parentUri = parentUri,
                folderUri = treeUri.toString(),
                sizeBytes = child.size,
            )
        }
    }

    /** ดึงรายการไฟล์/โฟลเดอร์ลูกทั้งหมดของโฟลเดอร์ในครั้งเดียว */
    private fun listChildren(treeUri: Uri, parentId: String): List<Child> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
        val projection = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
        )
        val list = mutableListOf<Child>()
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    val name = c.getString(1) ?: continue
                    if (name.startsWith(".")) continue // ข้ามไฟล์ซ่อน
                    list += Child(
                        docId = c.getString(0),
                        name = name,
                        isDir = c.getString(2) == Document.MIME_TYPE_DIR,
                        size = if (c.isNull(3)) 0 else c.getLong(3),
                    )
                }
            }
        }
        return list
    }

    companion object {
        /**
         * ทำชื่อไฟล์ให้อ่านง่าย: "Super Game (USA) [!].nes" → "Super Game"
         */
        fun cleanTitle(fileName: String): String {
            val noExt = fileName.substringBeforeLast('.')
            val cleaned = noExt
                .replace(Regex("""\s*[\(\[][^)\]]*[\)\]]"""), "")
                .replace('_', ' ')
                .trim()
            return cleaned.ifEmpty { noExt }
        }
    }
}
