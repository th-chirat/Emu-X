/*
 * SettingsRepository.kt
 * -----------------------------------------------------------------------------
 * เก็บ "ค่าตั้งค่า" ทั้งหมดของแอพด้วย DataStore (ไฟล์เล็กๆ ในเครื่อง)
 *
 *  - ค่าประสิทธิภาพ (ความลื่น) แยกตามเครื่อง
 *  - ค่า overlay FPS/CPU แยกตามเครื่อง
 *  - ผังปุ่มจอยที่ผู้ใช้ลากย้าย แยกตามเครื่อง
 *  - ข้อมูลโฆษณา (โฆษณาล่าสุดแสดงเมื่อไร / ซื้อลบโฆษณาแล้วหรือยัง)
 *
 * ข้อมูลที่ซับซ้อน (object) แปลงเป็น JSON ก่อนเก็บ เพื่อให้เพิ่มฟิลด์ได้ง่าย
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.allemulator.core.emulation.AppLanguage
import com.allemulator.core.emulation.ControllerLayout
import com.allemulator.core.emulation.ControllerLayouts
import com.allemulator.core.emulation.GameSystem
import com.allemulator.core.emulation.OverlaySettings
import com.allemulator.core.emulation.PerformanceSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store get() = context.dataStore

    /** ignoreUnknownKeys = อ่าน JSON เก่าได้แม้เราเพิ่ม/ลบฟิลด์ในเวอร์ชันใหม่ */
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ---------------- ตัวช่วยอ่าน/เขียน object เป็น JSON ----------------

    private fun <T> observeJson(key: String, serializer: KSerializer<T>, default: () -> T): Flow<T> =
        store.data.map { prefs ->
            prefs[stringPreferencesKey(key)]
                ?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }
                ?: default()
        }.distinctUntilChanged()

    private suspend fun <T> saveJson(key: String, serializer: KSerializer<T>, value: T) {
        store.edit { it[stringPreferencesKey(key)] = json.encodeToString(serializer, value) }
    }

    // ---------------- ประสิทธิภาพ ----------------

    fun performance(system: GameSystem): Flow<PerformanceSettings> =
        observeJson("perf_${system.id}", PerformanceSettings.serializer()) { PerformanceSettings() }

    suspend fun setPerformance(system: GameSystem, value: PerformanceSettings) =
        saveJson("perf_${system.id}", PerformanceSettings.serializer(), value)

    // ---------------- overlay ----------------

    fun overlay(system: GameSystem): Flow<OverlaySettings> =
        observeJson("overlay_${system.id}", OverlaySettings.serializer()) { OverlaySettings() }

    suspend fun setOverlay(system: GameSystem, value: OverlaySettings) =
        saveJson("overlay_${system.id}", OverlaySettings.serializer(), value)

    // ---------------- ผังปุ่มจอย ----------------

    fun layout(system: GameSystem): Flow<ControllerLayout> =
        observeJson("layout_${system.id}", ControllerLayout.serializer()) { ControllerLayouts.defaultFor(system) }

    suspend fun setLayout(system: GameSystem, value: ControllerLayout) =
        saveJson("layout_${system.id}", ControllerLayout.serializer(), value)

    /** รีเซ็ตผังปุ่มกลับเป็นค่าเริ่มต้น */
    suspend fun resetLayout(system: GameSystem) {
        store.edit { it.remove(stringPreferencesKey("layout_${system.id}")) }
    }

    // ---------------- ทั่วไป ----------------

    /** สั่นเมื่อกดปุ่มจอยเสมือน */
    val haptics: Flow<Boolean> = store.data.map { it[KEY_HAPTICS] ?: true }
    suspend fun setHaptics(on: Boolean) { store.edit { it[KEY_HAPTICS] = on } }

    /** ภาษาของแอพ (null = ยังไม่เคยเลือก → ใช้ตามภาษาเครื่อง) */
    val language: Flow<AppLanguage?> = store.data.map { AppLanguage.fromCode(it[KEY_LANGUAGE]) }.distinctUntilChanged()
    suspend fun setLanguage(lang: AppLanguage) { store.edit { it[KEY_LANGUAGE] = lang.code } }

    /** ผู้ใช้ดูหน้าสอนใช้งาน (ครั้งแรกที่เปิดแอพ) แล้วหรือยัง */
    val tutorialSeen: Flow<Boolean> = store.data.map { it[KEY_TUTORIAL_SEEN] ?: false }.distinctUntilChanged()
    suspend fun setTutorialSeen(seen: Boolean) { store.edit { it[KEY_TUTORIAL_SEEN] = seen } }

    // ---------------- โฆษณา / การซื้อ ----------------

    /** ผู้ใช้ซื้อ "ลบโฆษณาตลอดชีพ" แล้วหรือยัง (cache ไว้ เพื่อให้ไม่โชว์โฆษณาแม้ออฟไลน์) */
    val isPremium: Flow<Boolean> = store.data.map { it[KEY_PREMIUM] ?: false }.distinctUntilChanged()
    suspend fun setPremium(value: Boolean) { store.edit { it[KEY_PREMIUM] = value } }

    /** เวลา (epoch ms) ที่โฆษณาแสดงครั้งล่าสุด */
    suspend fun lastAdShownAt(): Long = store.data.first()[KEY_LAST_AD] ?: 0L
    suspend fun setLastAdShownAt(time: Long) { store.edit { it[KEY_LAST_AD] = time } }

    /** เวลาที่เปิดแอพครั้งแรก — ใช้ "ไม่โชว์โฆษณาในการเปิดครั้งแรก" ให้ผู้ใช้ใหม่ประทับใจก่อน */
    suspend fun firstLaunchAt(): Long {
        val existing = store.data.first()[KEY_FIRST_LAUNCH]
        if (existing != null) return existing
        val now = System.currentTimeMillis()
        store.edit { it[KEY_FIRST_LAUNCH] = now }
        return now
    }

    private companion object {
        val KEY_HAPTICS = booleanPreferencesKey("haptics")
        val KEY_PREMIUM = booleanPreferencesKey("premium")
        val KEY_LAST_AD = longPreferencesKey("last_ad_shown_at")
        val KEY_FIRST_LAUNCH = longPreferencesKey("first_launch_at")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_TUTORIAL_SEEN = booleanPreferencesKey("tutorial_seen")
    }
}
