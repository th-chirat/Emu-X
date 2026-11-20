/*
 * PerformancePreset.kt
 * -----------------------------------------------------------------------------
 * "โหมดความลื่น" 3 ระดับที่ผู้ใช้เลือกได้ง่ายๆ โดยไม่ต้องรู้เรื่องเทคนิค
 * ระบบจะแปลงโหมดนี้เป็นค่าตั้งค่าของ core จริงใน CoreOptions.kt
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

import kotlinx.serialization.Serializable

@Serializable
enum class PerformancePreset(val title: String, val description: String) {
    BATTERY(
        "ประหยัดแบต",
        "ความละเอียดเดิมของเครื่อง + ข้ามเฟรมอัตโนมัติ เหมาะกับมือถือสเปคต่ำ/อยากเล่นนาน",
    ),
    BALANCED(
        "สมดุล (แนะนำ)",
        "ภาพชัดขึ้น 2 เท่า ลื่นในมือถือส่วนใหญ่",
    ),
    PERFORMANCE(
        "ภาพสวยสุด",
        "ความละเอียด 3 เท่า ต้องใช้มือถือสเปคสูง เครื่องอาจร้อน",
    ),
}

/** ฟิลเตอร์ภาพที่ LibretroDroid มีให้ */
@Serializable
enum class ScreenShader(val title: String) {
    NONE("ปกติ"),
    SHARP("คมชัด"),
    LCD("จอ LCD"),
    CRT("จอทีวีเก่า (CRT)"),
}

/**
 * ค่าตั้งค่าประสิทธิภาพของเครื่องเกมหนึ่ง (ผู้ใช้ปรับในหน้า Settings)
 *
 * @property preset           โหมดความลื่นหลัก
 * @property frameSkip        ข้ามเฟรม: 0 = ปิด, -1 = อัตโนมัติ, 1..3 = ข้ามแบบคงที่
 * @property resolutionScale  ตัวคูณความละเอียด (1..4); null = ตาม preset
 * @property threadedRender   เรนเดอร์ภาพแยก thread (ช่วยมือถือหลายคอร์)
 * @property lowLatencyAudio  เสียงหน่วงต่ำ (ถ้าเสียงแตกให้ปิด)
 * @property shader           ฟิลเตอร์ภาพ
 */
@Serializable
data class PerformanceSettings(
    val preset: PerformancePreset = PerformancePreset.BALANCED,
    val frameSkip: Int = 0,
    val resolutionScale: Int? = null,
    val threadedRender: Boolean = true,
    val lowLatencyAudio: Boolean = true,
    val shader: ScreenShader = ScreenShader.NONE,
)

/** มุมที่จะวาง overlay FPS/CPU/GPU */
@Serializable
enum class OverlayCorner(val title: String) { TOP_START("ซ้ายบน"), TOP_END("ขวาบน"), BOTTOM_START("ซ้ายล่าง"), BOTTOM_END("ขวาล่าง") }

/**
 * ค่าตั้งค่า overlay ข้อมูลประสิทธิภาพ (เลือกเปิดทีละอย่างได้)
 */
@Serializable
data class OverlaySettings(
    val showFps: Boolean = false,
    val showCpu: Boolean = false,
    val showGpu: Boolean = false,
    val showRam: Boolean = false,
    val showBatteryTemp: Boolean = false,
    val corner: OverlayCorner = OverlayCorner.TOP_START,
) {
    val anyEnabled get() = showFps || showCpu || showGpu || showRam || showBatteryTemp
}
