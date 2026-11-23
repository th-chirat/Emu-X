/*
 * PerformanceMonitor.kt
 * -----------------------------------------------------------------------------
 * วัดค่าประสิทธิภาพเพื่อโชว์ใน overlay (บรีฟข้อ 1.2.1 — GPU ตัดออกตามบรีฟรอบ 2)
 *
 *   FPS  — นับเฟรมที่ emulator วาดเสร็จในแต่ละวินาที (แม่นยำ)
 *   CPU  — อ่าน /proc/self/stat ดูว่าแอพใช้ CPU ไปกี่ % ของทุกคอร์ (แม่นยำ)
 *   RAM  — หน่วยความจำที่แอพใช้จริง (RSS)
 *   TEMP — อุณหภูมิแบตเตอรี่ (บอกได้ว่าเครื่องร้อนแค่ไหน)
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock
import java.io.File

/** ค่าที่วัดได้ ณ วินาทีนั้น */
data class PerfSnapshot(
    val fps: Int = 0,
    val cpuPercent: Int? = null,
    val frameTimeMs: Float = 0f,
    val ramMb: Int? = null,
    val batteryTempC: Float? = null,
)

class PerformanceMonitor(private val context: Context) {

    private var frames = 0
    private var windowStart = SystemClock.elapsedRealtime()
    private var lastCpuTicks = readCpuTicks()
    private var lastCpuTime = SystemClock.elapsedRealtime()
    private val cores = Runtime.getRuntime().availableProcessors()

    /** เรียกทุกครั้งที่ emulator วาดเฟรมเสร็จ */
    fun onFrame() { frames++ }

    /**
     * สรุปค่าของช่วงที่ผ่านมา (เรียกทุก ~1 วินาที) แล้วเริ่มนับใหม่
     */
    fun sample(): PerfSnapshot {
        val now = SystemClock.elapsedRealtime()
        val elapsed = (now - windowStart).coerceAtLeast(1)
        val fps = (frames * 1000f / elapsed).toInt()
        frames = 0
        windowStart = now

        return PerfSnapshot(
            fps = fps,
            cpuPercent = cpuPercent(now),
            frameTimeMs = if (fps > 0) 1000f / fps else 0f,
            ramMb = ramMb(),
            batteryTempC = batteryTemp(),
        )
    }

    // ---------------- CPU ----------------

    /** utime + stime ของ process (หน่วย clock tick, ปกติ 100 tick = 1 วินาที) */
    private fun readCpuTicks(): Long? = runCatching {
        val stat = File("/proc/self/stat").readText()
        // ข้ามชื่อ process ที่อยู่ในวงเล็บ (อาจมีช่องว่าง) แล้วค่อย split
        val fields = stat.substringAfterLast(')').trim().split(' ')
        fields[11].toLong() + fields[12].toLong() // utime, stime
    }.getOrNull()

    private fun cpuPercent(now: Long): Int? {
        val ticks = readCpuTicks() ?: return null
        val prev = lastCpuTicks ?: run { lastCpuTicks = ticks; return null }
        val cpuMs = (ticks - prev) * 10f // 1 tick = 10 ms
        val wallMs = (now - lastCpuTime).coerceAtLeast(1)
        lastCpuTicks = ticks
        lastCpuTime = now
        return (cpuMs / (wallMs * cores) * 100).toInt().coerceIn(0, 100)
    }

    // ---------------- RAM / อุณหภูมิ ----------------

    private fun ramMb(): Int? = runCatching {
        // statm: ช่องที่ 2 = จำนวนหน้า memory ที่ใช้จริง (1 หน้า = 4 KB)
        val pages = File("/proc/self/statm").readText().trim().split(' ')[1].toLong()
        (pages * 4 / 1024).toInt()
    }.getOrNull()

    private fun batteryTemp(): Float? {
        val intent: Intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return null
        val tenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        return if (tenths == Int.MIN_VALUE) null else tenths / 10f
    }
}
