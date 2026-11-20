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
import com.allemulator.core.data.db.FolderEntity
import com.allemulator.core.data.db.GameDao
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.data.scan.GameScanner
import com.allemulator.core.emulation.GameSystem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    suspend fun markPlayed(uri: String) = dao.markPlayed(uri, System.currentTimeMillis())
    suspend fun toggleFavorite(uri: String) = dao.toggleFavorite(uri)
    suspend fun hide(uri: String) = dao.delete(uri)
}
