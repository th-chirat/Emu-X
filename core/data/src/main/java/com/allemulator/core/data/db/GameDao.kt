/*
 * GameDao.kt
 * -----------------------------------------------------------------------------
 * DAO = "Data Access Object" — รวมคำสั่ง SQL ที่ใช้อ่าน/เขียนตาราง games และ folders
 * ฟังก์ชันที่คืน Flow จะ "อัปเดตอัตโนมัติ" เมื่อข้อมูลในตารางเปลี่ยน
 * ทำให้หน้าจอรีเฟรชเองโดยไม่ต้องสั่ง
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** จำนวนเกมของแต่ละเครื่อง (ใช้โชว์บนการ์ดหน้าแรก) */
data class SystemCount(val systemId: String, val count: Int)

@Dao
interface GameDao {

    // ---------------- เกม ----------------

    /** เกมทั้งหมดของเครื่องหนึ่ง: ติดดาวก่อน → เล่นล่าสุด → ตามชื่อ */
    @Query(
        """
        SELECT * FROM games WHERE systemId = :systemId
        ORDER BY favorite DESC, lastPlayedAt DESC, title COLLATE NOCASE ASC
        """,
    )
    fun observeGames(systemId: String): Flow<List<GameEntity>>

    @Query("SELECT systemId, COUNT(*) AS count FROM games GROUP BY systemId")
    fun observeCounts(): Flow<List<SystemCount>>

    /** เกมที่เล่นล่าสุด (ทุกเครื่อง) — โชว์แถว "เล่นต่อ" บนหน้าแรก */
    @Query("SELECT * FROM games WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE uri = :uri")
    suspend fun get(uri: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(games: List<GameEntity>)

    @Query("UPDATE games SET lastPlayedAt = :time WHERE uri = :uri")
    suspend fun markPlayed(uri: String, time: Long)

    @Query("UPDATE games SET favorite = NOT favorite WHERE uri = :uri")
    suspend fun toggleFavorite(uri: String)

    @Query("DELETE FROM games WHERE uri = :uri")
    suspend fun delete(uri: String)

    @Query("SELECT uri FROM games WHERE folderUri = :folderUri AND systemId = :systemId")
    suspend fun urisInFolder(systemId: String, folderUri: String): List<String>

    @Query("DELETE FROM games WHERE uri IN (:uris)")
    suspend fun deleteAll(uris: List<String>)

    /**
     * แทนที่รายการเกมในโฟลเดอร์หนึ่งด้วยผลสแกนใหม่:
     * เกมใหม่ → เพิ่ม, เกมที่หายไปจากโฟลเดอร์ → ลบ, เกมเดิม → เก็บสถิติการเล่นไว้
     */
    @Transaction
    suspend fun syncFolder(systemId: String, folderUri: String, scanned: List<GameEntity>) {
        val existing = urisInFolder(systemId, folderUri).toSet()
        val found = scanned.map { it.uri }.toSet()
        deleteAll((existing - found).toList())
        insertIgnore(scanned.filter { it.uri !in existing })
    }

    // ---------------- โฟลเดอร์ ----------------

    @Query("SELECT * FROM folders WHERE systemId = :systemId")
    fun observeFolders(systemId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders")
    suspend fun allFolders(): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE systemId = :systemId AND treeUri = :treeUri")
    suspend fun deleteFolder(systemId: String, treeUri: String)

    @Query("DELETE FROM games WHERE systemId = :systemId AND folderUri = :treeUri")
    suspend fun deleteGamesInFolder(systemId: String, treeUri: String)
}
