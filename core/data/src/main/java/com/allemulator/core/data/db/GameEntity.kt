/*
 * GameEntity.kt
 * -----------------------------------------------------------------------------
 * ตารางในฐานข้อมูล (Room) — 1 แถว = เกม 1 เกมที่สแกนเจอ หรือ 1 โฟลเดอร์ที่ผู้ใช้เพิ่ม
 *
 * ทำไมเก็บเป็น URI ไม่ใช่ path?
 *   ตั้งแต่ Android 10 แอพอ่านไฟล์ตรงๆ ด้วย path ไม่ได้แล้ว ต้องขอสิทธิ์โฟลเดอร์ผ่าน
 *   Storage Access Framework (SAF) ซึ่งให้ "content://..." URI มาแทน
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * เกม 1 เกม
 *
 * @property uri          URI ของไฟล์เกม (ไม่ซ้ำ ใช้เป็น primary key)
 * @property systemId     เครื่องเกม (GameSystem.id)
 * @property title        ชื่อเกมที่โชว์ (ตัดนามสกุลและวงเล็บ region ออก)
 * @property fileName     ชื่อไฟล์จริง (มีนามสกุล — core บางตัวต้องใช้)
 * @property parentUri    URI ของโฟลเดอร์ที่ไฟล์นี้อยู่ (ใช้หาไฟล์ข้างเคียง เช่น .bin ของ .cue)
 * @property folderUri    โฟลเดอร์ "ราก" ที่ผู้ใช้เลือกตอนกด + (ใช้ตอนลบโฟลเดอร์/สแกนใหม่)
 * @property sizeBytes    ขนาดไฟล์
 * @property lastPlayedAt เวลาที่เล่นล่าสุด (epoch ms) 0 = ยังไม่เคยเล่น
 * @property favorite     ติดดาวไว้หรือไม่
 */
@Entity(tableName = "games", indices = [Index("systemId"), Index("folderUri")])
data class GameEntity(
    @PrimaryKey val uri: String,
    val systemId: String,
    val title: String,
    val fileName: String,
    val parentUri: String,
    val folderUri: String,
    val sizeBytes: Long,
    val lastPlayedAt: Long = 0,
    val favorite: Boolean = false,
)

/**
 * โฟลเดอร์เกมที่ผู้ใช้เลือกไว้ (1 เครื่องมีได้หลายโฟลเดอร์)
 */
@Entity(tableName = "folders", primaryKeys = ["systemId", "treeUri"])
data class FolderEntity(
    val systemId: String,
    val treeUri: String,
    val addedAt: Long = System.currentTimeMillis(),
)
