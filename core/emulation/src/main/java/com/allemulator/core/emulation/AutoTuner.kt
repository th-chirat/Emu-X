/*
 * AutoTuner.kt
 * -----------------------------------------------------------------------------
 * "ปรับความลื่นอัตโนมัติ" — เป้าหมาย: เกมต้องได้อย่างน้อย ~28 FPS
 *
 * ทำงานยังไง:
 *   หน้าเล่นเกมส่ง FPS เข้ามาทุก 1 วินาที → ถ้าค่าเฉลี่ย 6 วินาทีล่าสุดต่ำกว่าเป้า
 *   จะลดการตั้งค่าลงทีละขั้น (ขั้นละครั้ง แล้วรอดูผล 8 วินาทีก่อนลดต่อ):
 *     1) ลดความละเอียด (PSP / PS1) ทีละ 1 ขั้น จนเหลือความละเอียดเดิมของเครื่อง
 *     2) เปิดทางลัดเร่งความเร็ว (speedHacks) ของ emulator
 *     3) เปิดข้ามเฟรมอัตโนมัติ — เกมยังเดินความเร็วปกติ แต่วาดภาพน้อยลง
 *   ช่วง 10 วินาทีแรกไม่นับ (ตอนโหลดเกม/โลโก้ FPS ต่ำเป็นปกติ)
 *
 * แยกเป็นคลาสธรรมดา (ไม่ผูกกับ Android) เพื่อให้เขียน unit test ได้
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

class AutoTuner(
    private val system: GameSystem,
    /** FPS ขั้นต่ำที่ต้องการ (ผู้ใช้ขอ 25-30 → ใช้ 28) */
    private val targetFps: Int = 28,
    /** จำนวนวินาทีที่ใช้หาค่าเฉลี่ย */
    private val windowSec: Int = 6,
    /** วินาทีแรกที่ไม่นับ (ตอนโหลดเกม) */
    warmupSec: Int = 10,
    /** หลังปรับแล้ว รอกี่วินาทีก่อนตัดสินใจรอบถัดไป */
    private val settleSec: Int = 8,
) {
    /** ผลการปรับ 1 ครั้ง: ค่าตั้งค่าใหม่ + ข้อความบอกผู้ใช้ */
    data class Step(val settings: PerformanceSettings, val message: String)

    private val samples = ArrayDeque<Int>()
    private var waitSec = warmupSec

    /** true = ปรับจนสุดแล้ว ไม่มีอะไรให้ลดอีก */
    var exhausted = false
        private set

    /**
     * ป้อน FPS ของวินาทีที่ผ่านมา
     * @return ค่าตั้งค่าใหม่ถ้าควรลดลง, null = ไม่ต้องทำอะไร
     */
    fun onSample(fps: Int, current: PerformanceSettings): Step? {
        if (!current.autoTune || exhausted) return null
        if (waitSec > 0) { waitSec--; return null }
        samples.addLast(fps)
        if (samples.size > windowSec) samples.removeFirst()
        if (samples.size < windowSec || samples.average() >= targetFps) return null

        samples.clear()
        waitSec = settleSec
        return nextLighter(current).also { if (it == null) exhausted = true }
    }

    /** เริ่มนับใหม่ (เช่น หลังปิดเมนูหยุดเกม หรือเลิกเร่งความเร็ว) */
    fun reset(settle: Int = settleSec) {
        samples.clear()
        waitSec = settle
    }

    /** ขั้นถัดไปที่เบาลง (null = ไม่มีให้ลดแล้ว) */
    fun nextLighter(s: PerformanceSettings): Step? {
        val scale = CoreOptions.scaleOf(s)
        if (system in SCALABLE && scale > 1) {
            return Step(s.copy(resolutionScale = scale - 1), tr("FPS ต่ำ → ลดความละเอียดเป็น ${scale - 1}x อัตโนมัติ", "Low FPS → resolution lowered to ${scale - 1}x automatically"))
        }
        if (system in HACKABLE && !s.speedHacks) {
            return Step(s.copy(speedHacks = true), tr("FPS ต่ำ → เปิดโหมดเร่งความเร็วอัตโนมัติ", "Low FPS → speed hacks turned on automatically"))
        }
        if (system in SKIPPABLE && s.frameSkip == 0) {
            return Step(s.copy(frameSkip = -1), tr("FPS ต่ำ → เปิดข้ามเฟรมอัตโนมัติ (เกมยังเดินความเร็วปกติ)", "Low FPS → frame skip turned on (game still runs at full speed)"))
        }
        return null
    }

    private companion object {
        /** เครื่องที่ปรับความละเอียดได้ */
        val SCALABLE = setOf(GameSystem.PSP, GameSystem.PS1)
        /** เครื่องที่มีทางลัดเร่งความเร็ว (ดู CoreOptions) */
        val HACKABLE = setOf(GameSystem.PSP)
        /** เครื่องที่ข้ามเฟรมได้ */
        val SKIPPABLE = setOf(GameSystem.PSP, GameSystem.PS1, GameSystem.GBA)
    }
}
