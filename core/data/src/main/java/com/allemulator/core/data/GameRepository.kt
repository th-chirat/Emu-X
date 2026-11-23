/*
 * GameRepository.kt
 * -----------------------------------------------------------------------------
 * "ประตูเดียว" สำหรับทุกอย่างเกี่ยวกับรายการเกม — หน้าจอต่างๆ คุยกับคลาสนี้
 * ไม่ต้องรู้ว่าข้างหลังใช้ Room หรือ SAF
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.allemulator.core.data.db.FolderEntity
import com.allemulator.core.data.db.GameDao
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.data.scan.GameScanner
import com.allemulator.core.emulation.GameSystem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: GameDao,
    private val scanner: GameScanner,
) {
    /** รายการเกมของเครื่องหนึ่ง (อัปเดตอัตโนมัติ) */
    fun games(system: GameSystem): Flow<List<GameEntity>> = dao.observeGames(system.id)

    /** จำนวนเกมของทุกเครื่อง เช่น {PSP=12, NES=40} */
    fun counts(): Flow<Map<GameSystem, Int>> = dao.observeCounts().map { rows ->
        rows.mapNotNull { r -> GameSystem.fromId(r.systemId)?.let { it to r.count } }.toMap()
    }

    /** เกมที่เล่นล่าสุด */
    fun recent(limit: Int = 10): Flow<List<GameEntity>> = dao.observeRecent(limit)

    fun folders(system: GameSystem): Flow<List<FolderEntity>> = dao.observeFolders(system.id)

    suspend fun game(uri: String): GameEntity? = dao.get(uri)

    /**
     * ผู้ใช้เลือกโฟลเดอร์ใหม่จากปุ่ม +
     * 1) ขอสิทธิ์อ่านโฟลเดอร์ "ถาวร" (ไม่งั้นรีสตาร์ทเครื่องแล้วสิทธิ์หาย)
     * 2) บันทึกโฟลเดอร์ 3) สแกนหาเกม
     * @return จำนวนเกมที่เจอ
     */
    suspend fun addFolder(system: GameSystem, treeUri: Uri): Int {
        context.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        dao.insertFolder(FolderEntity(system.id, treeUri.toString()))
        return rescanFolder(system, treeUri)
    }

    /** สแกนโฟลเดอร์ซ้ำ (เช่น ผู้ใช้เพิ่งก็อปเกมใหม่ลงไป) */
    suspend fun rescanFolder(system: GameSystem, treeUri: Uri): Int {
        val found = scanner.scan(system, treeUri)
        dao.syncFolder(system.id, treeUri.toString(), found)
        return found.size
    }

    /** สแกนทุกโฟลเดอร์ของเครื่องนี้ใหม่ — ใช้กับปุ่ม "รีเฟรช" */
    suspend fun rescanSystem(system: GameSystem) {
        dao.allFolders().filter { it.systemId == system.id }.forEach {
            runCatching { rescanFolder(system, Uri.parse(it.treeUri)) }
        }
    }

    /** เอาโฟลเดอร์ออก (ไม่ลบไฟล์จริง แค่เลิกโชว์ในแอพ) */
    suspend fun removeFolder(system: GameSystem, treeUri: String) {
        dao.deleteGamesInFolder(system.id, treeUri)
        dao.deleteFolder(system.id, treeUri)
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                Uri.parse(treeUri),
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    /**
     * ผู้ใช้เลือก "ไฟล์" (ไม่ใช่โฟลเดอร์) จากปุ่ม + → เลือกไฟล์
     *   - ไฟล์ .zip → แตกไฟล์อัตโนมัติไปไว้ในพื้นที่ของแอพ (files/imported/<เครื่อง>/<ชื่อ zip>/)
     *     แล้วเพิ่มเกมที่เจอข้างในเข้ารายการ (ลบไฟล์ zip ต้นฉบับทิ้งได้เลยหลังจากนี้)
     *   - ไฟล์เกมปกติ → ขอสิทธิ์อ่านถาวร แล้วเพิ่มเข้ารายการตรงๆ (ไม่ก๊อป)
     * @return จำนวนเกมที่เพิ่ม
     */
    suspend fun importFiles(system: GameSystem, uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val added = mutableListOf<GameEntity>()
        for (uri in uris) {
            val name = displayName(uri) ?: continue
            if (name.substringAfterLast('.', "").lowercase() == "zip") {
                added += extractImportedZip(system, uri, name)
            } else if (system.accepts(name)) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                added += GameEntity(
                    uri = uri.toString(), systemId = system.id, title = GameScanner.cleanTitle(name),
                    fileName = name, parentUri = "", folderUri = IMPORTED, sizeBytes = fileSize(uri),
                )
            }
        }
        dao.insertIgnore(added)
        added.size
    }

    /** แตก zip ทั้งไฟล์ลงโฟลเดอร์ของแอพ แล้วคืนรายการเกมที่อยู่ข้างใน */
    private fun extractImportedZip(system: GameSystem, uri: Uri, zipName: String): List<GameEntity> {
        val dir = File(context.filesDir, "imported/${system.id}/${zipName.substringBeforeLast('.')}")
        dir.deleteRecursively()
        dir.mkdirs()
        val input = context.contentResolver.openInputStream(uri) ?: return emptyList()
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.isDirectory) continue
                // ข้ามไฟล์ขยะที่ Mac ใส่มาใน zip (__MACOSX/ และไฟล์ชื่อขึ้นต้น "._") — ไม่ใช่เกมจริง
                val base = entry.name.substringAfterLast('/')
                if (entry.name.startsWith("__MACOSX/") || base.startsWith(".")) continue
                val target = File(dir, entry.name.substringAfterLast('/'))
                // กันไฟล์ zip ที่ตั้งชื่อแบบ "../" เพื่อเขียนทับไฟล์นอกโฟลเดอร์ (zip slip)
                if (!target.canonicalPath.startsWith(dir.canonicalPath)) continue
                target.outputStream().use { zip.copyTo(it, 1 shl 20) }
            }
        }
        val files = dir.listFiles().orEmpty().filter { system.accepts(it.name) && !it.name.endsWith(".zip", true) }
        // ถ้ามี .cue/.m3u ไม่ต้องโชว์ .bin/.img ที่เป็นไส้ใน (เหมือนตอนสแกนโฟลเดอร์)
        val hasPlaylist = files.any { it.extension.lowercase() in setOf("cue", "m3u") }
        val games = files.filterNot { hasPlaylist && it.extension.lowercase() in setOf("bin", "img") }
        if (games.isEmpty()) dir.deleteRecursively()
        return games.map { f ->
            GameEntity(
                uri = Uri.fromFile(f).toString(), systemId = system.id, title = GameScanner.cleanTitle(f.name),
                fileName = f.name, parentUri = Uri.fromFile(dir).toString(), folderUri = IMPORTED, sizeBytes = f.length(),
            )
        }
    }

    private fun displayName(uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }

    private fun fileSize(uri: Uri): Long =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else 0L
        } ?: 0L

    suspend fun markPlayed(uri: String) = dao.markPlayed(uri, System.currentTimeMillis())
    suspend fun toggleFavorite(uri: String) = dao.toggleFavorite(uri)
    /** เอาเกมออกจากรายการ — ถ้าเป็นเกมที่แตกจาก zip ไว้ในแอพ ลบไฟล์ทิ้งด้วย (คืนพื้นที่) */
    suspend fun hide(uri: String) {
        dao.delete(uri)
        val parsed = Uri.parse(uri)
        if (parsed.scheme == "file") withContext(Dispatchers.IO) {
            val file = File(parsed.path ?: return@withContext)
            val imported = File(context.filesDir, "imported").canonicalPath
            if (file.canonicalPath.startsWith(imported)) {
                file.delete()
                // โฟลเดอร์ที่เหลือแค่ไฟล์ข้างเคียง (เช่น .bin) และไม่มีเกมอื่นแล้ว → ลบทั้งโฟลเดอร์
                val dir = file.parentFile
                if (dir != null && dir.listFiles().orEmpty().none { f -> GameSystem.entries.any { s -> s.accepts(f.name) } && f.extension !in setOf("bin", "img") }) {
                    dir.deleteRecursively()
                }
            }
        }
    }

    companion object {
        /** ค่า folderUri ของเกมที่เพิ่มแบบ "เลือกไฟล์" (ไม่ได้มาจากโฟลเดอร์) */
        const val IMPORTED = "imported"
    }
}
