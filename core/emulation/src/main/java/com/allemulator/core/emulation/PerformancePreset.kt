/*
 * PerformancePreset.kt
 * -----------------------------------------------------------------------------
 * "โหมดความลื่น" 3 ระดับที่ผู้ใช้เลือกได้ง่ายๆ โดยไม่ต้องรู้เรื่องเทคนิค
 * ระบบจะแปลงโหมดนี้เป็นค่าตั้งค่าของ core จริงใน CoreOptions.kt
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

import kotlinx.serialization.Serializable

// ข้อความแต่ละค่ามี 2 ภาษา: title/description เป็น getter จึงเปลี่ยนตามภาษาที่เลือกทันที
@Serializable
enum class PerformancePreset(
    private val titleTh: String,
    private val titleEn: String,
    private val descTh: String,
    private val descEn: String,
) {
    BATTERY(
        "ประหยัดแบต", "Battery saver",
        "ความละเอียดเดิมของเครื่อง + ข้ามเฟรมอัตโนมัติ เหมาะกับมือถือสเปคต่ำ/อยากเล่นนาน",
        "Native resolution + auto frame skip. Best for low-end phones / long sessions",
    ),
    BALANCED(
        "สมดุล (แนะนำ)", "Balanced (recommended)",
        "ภาพชัดขึ้น 2 เท่า ลื่นในมือถือส่วนใหญ่",
        "2x sharper picture, smooth on most phones",
    ),
    PERFORMANCE(
        "ภาพสวยสุด", "Best quality",
        "ความละเอียด 3 เท่า ต้องใช้มือถือสเปคสูง เครื่องอาจร้อน",
        "3x resolution, needs a high-end phone, may run hot",
    ),
    ;

    val title: String get() = tr(titleTh, titleEn)
    val description: String get() = tr(descTh, descEn)
}

/** ฟิลเตอร์ภาพที่ LibretroDroid มีให้ */
@Serializable
enum class ScreenShader(private val th: String, private val en: String) {
    NONE("ปกติ", "Normal"),
    SHARP("คมชัด", "Sharp"),
    LCD("จอ LCD", "LCD"),
    CRT("จอทีวีเก่า (CRT)", "Old TV (CRT)"),
    ;

    val title: String get() = tr(th, en)
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
 * @property speedHacks       เปิดทางลัดเร่งความเร็วของ emulator (เร็วขึ้น แต่บางเกมภาพอาจเพี้ยนเล็กน้อย)
 * @property autoTune         ปรับความลื่นอัตโนมัติ: ถ้า FPS ต่ำกว่าเป้า ลดความละเอียด/ข้ามเฟรมให้เอง (ดู AutoTuner)
 */
@Serializable
data class PerformanceSettings(
    val preset: PerformancePreset = PerformancePreset.BALANCED,
    val frameSkip: Int = 0,
    val resolutionScale: Int? = null,
    val threadedRender: Boolean = true,
    val lowLatencyAudio: Boolean = true,
    val shader: ScreenShader = ScreenShader.NONE,
    val speedHacks: Boolean = false,
    val autoTune: Boolean = true,
)

/** มุมที่จะวาง overlay FPS/CPU */
@Serializable
enum class OverlayCorner(private val th: String, private val en: String) {
    TOP_START("ซ้ายบน", "Top left"),
    TOP_END("ขวาบน", "Top right"),
    BOTTOM_START("ซ้ายล่าง", "Bottom left"),
    BOTTOM_END("ขวาล่าง", "Bottom right"),
    ;

    val title: String get() = tr(th, en)
}

/**
 * ค่าตั้งค่า overlay ข้อมูลประสิทธิภาพ (เลือกเปิดทีละอย่างได้)
 */
@Serializable
data class OverlaySettings(
    val showFps: Boolean = false,
    val showCpu: Boolean = false,
    val showRam: Boolean = false,
    val showBatteryTemp: Boolean = false,
    val corner: OverlayCorner = OverlayCorner.TOP_START,
) {
    val anyEnabled get() = showFps || showCpu || showRam || showBatteryTemp
}
