/*
 * CoreOptionsTest.kt — ทดสอบว่า preset แปลงเป็น core options ถูกต้อง
 * รันด้วยคำสั่ง: ./gradlew :core:emulation:testDebugUnitTest
 */
package com.allemulator.core.emulation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreOptionsTest {

    @Test
    fun psp_battery_preset_uses_native_resolution_and_auto_frameskip() {
        val o = CoreOptions.build(GameSystem.PSP, PerformanceSettings(preset = PerformancePreset.BATTERY))
        assertEquals("480x272", o["ppsspp_internal_resolution"])
        assertEquals("enabled", o["ppsspp_auto_frameskip"])
    }

    @Test
    fun manual_resolution_overrides_preset() {
        val o = CoreOptions.build(GameSystem.PSP, PerformanceSettings(resolutionScale = 4))
        assertEquals("1920x1088", o["ppsspp_internal_resolution"])
    }

    @Test
    fun ps2_never_exceeds_2x_on_mobile() {
        val o = CoreOptions.build(GameSystem.PS2, PerformanceSettings(preset = PerformancePreset.PERFORMANCE, resolutionScale = 4))
        assertEquals("2x", o["play_res_multi"])
    }

    @Test
    fun light_systems_have_no_options() {
        assertTrue(CoreOptions.build(GameSystem.NES, PerformanceSettings()).isEmpty())
    }

    @Test
    fun extension_matching_is_case_insensitive() {
        assertTrue(GameSystem.GBC.accepts("Pokemon Crystal.GBC"))
    }
}
