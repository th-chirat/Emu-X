/*
 * CoreOptions.kt
 * -----------------------------------------------------------------------------
 * แปลง "โหมดความลื่น" (ภาษาคน) → "core options" (ภาษาของ emulator แต่ละตัว)
 *
 * ชื่อ key และค่าทั้งหมดตรวจจาก source code ของแต่ละ core แล้ว:
 *   PPSSPP       → ppsspp/libretro/libretro_core_options.h
 *   PCSX-ReARMed → pcsx_rearmed/frontend/libretro_core_options.h
 *   mGBA         → mgba/src/platform/libretro/libretro_core_options.h
 * ถ้า core อัปเดตแล้วเปลี่ยนชื่อ key ต้องมาแก้ที่ไฟล์นี้
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

object CoreOptions {

    /**
     * คืนค่า core options (key → value) สำหรับเครื่องเกม + การตั้งค่าที่ระบุ
     * ค่าที่ core ไม่รู้จักจะถูก core เพิกเฉยเอง จึงไม่ทำให้เกมพัง
     */
    fun build(system: GameSystem, s: PerformanceSettings): Map<String, String> {
        val scale = scaleOf(s)
        val autoSkip = s.frameSkip == -1 || (s.frameSkip == 0 && s.preset == PerformancePreset.BATTERY)

        return when (system) {
            GameSystem.PSP -> buildMap {
                put("ppsspp_cpu_core", "JIT") // JIT เร็วที่สุดบน ARM
                put("ppsspp_fast_memory", "enabled")
                put("ppsspp_internal_resolution", pspResolution(scale))
                put("ppsspp_auto_frameskip", if (autoSkip) "enabled" else "disabled")
                put("ppsspp_frameskip", if (s.frameSkip > 0) s.frameSkip.toString() else "disabled")
                put("ppsspp_texture_scaling_level", "disabled") // กิน GPU มาก ปิดไว้
                put("ppsspp_skip_buffer_effects", if (s.preset == PerformancePreset.BATTERY) "enabled" else "disabled")
                // ทางลัดเร่งความเร็ว: ไม่อ่านภาพกลับจาก GPU + ลดความละเอียดของ effect (ช่วยมากบนมือถือ)
                put("ppsspp_skip_gpu_readbacks", if (s.speedHacks) "enabled" else "disabled")
                put("ppsspp_lower_resolution_for_effects", if (s.speedHacks) "Aggressive" else "disabled")
            }

            GameSystem.PS1 -> buildMap {
                put("pcsx_rearmed_drc", "enabled") // dynarec = แปลงโค้ด PS1 เป็นโค้ด ARM ให้เร็วขึ้น
                put("pcsx_rearmed_gpu_thread_rendering", if (s.threadedRender) "enabled" else "disabled")
                put(
                    "pcsx_rearmed_frameskip_type",
                    when {
                        autoSkip -> "auto"
                        s.frameSkip > 0 -> "fixed_interval"
                        else -> "disabled"
                    },
                )
                if (s.frameSkip > 0) put("pcsx_rearmed_frameskip_interval", s.frameSkip.toString())
                // โหมดความละเอียดสูง (2x) ของ PS1 — เปิดเมื่อ scale >= 2
                put("pcsx_rearmed_neon_enhancement_enable", if (scale >= 2) "enabled" else "disabled")
            }

            // GBA: mGBA เบาอยู่แล้ว — โหมดประหยัดแบตให้ข้ามเฟรมเมื่อเครื่องช้า
            // (key จาก mgba/src/platform/libretro/libretro_core_options.h)
            GameSystem.GBA -> buildMap {
                put("mgba_frameskip", if (autoSkip) "auto" else "disabled")
                put("mgba_skip_bios", "ON") // ข้ามโลโก้ตอนเปิดเครื่อง (ใช้ได้เมื่อมี BIOS จริง)
            }

            // GB / GBC / NES เบามาก ไม่ต้องปรับอะไร
            GameSystem.GB, GameSystem.GBC, GameSystem.NES -> emptyMap()
        }
    }

    /** ตัวคูณความละเอียดที่ใช้จริง: ถ้าผู้ใช้ไม่ได้กำหนดเอง ใช้ค่าจาก preset */
    fun scaleOf(s: PerformanceSettings): Int = (s.resolutionScale ?: when (s.preset) {
        PerformancePreset.BATTERY -> 1
        PerformancePreset.BALANCED -> 2
        PerformancePreset.PERFORMANCE -> 3
    }).coerceIn(1, 4)

    private fun pspResolution(scale: Int) = when (scale) {
        1 -> "480x272"
        2 -> "960x544"
        3 -> "1440x816"
        else -> "1920x1088"
    }
}
