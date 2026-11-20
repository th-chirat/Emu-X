/*
 * AppDatabase.kt
 * -----------------------------------------------------------------------------
 * ตัวฐานข้อมูล Room ของแอพ (ไฟล์ SQLite ชื่อ all_emulator.db ในเครื่องผู้ใช้)
 *
 * ถ้าแก้โครงสร้างตาราง (เพิ่ม/ลบคอลัมน์) ต้องเพิ่ม version + เขียน Migration
 * ไม่งั้นแอพที่ผู้ใช้ติดตั้งไว้แล้วจะ crash ตอนอัปเดต
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GameEntity::class, FolderEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
