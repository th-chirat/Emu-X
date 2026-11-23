package com.allemulator.core.emulation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoTunerTest {

    private fun feed(t: AutoTuner, fps: Int, s: PerformanceSettings, secs: Int): AutoTuner.Step? {
        repeat(secs) { t.onSample(fps, s)?.let { return it } }
        return null
    }

    @Test
    fun `เกมลื่นอยู่แล้ว ไม่ต้องปรับ`() {
        val t = AutoTuner(GameSystem.PSP)
        assertNull(feed(t, 60, PerformanceSettings(), 60))
    }

    @Test
    fun `ช่วงโหลดเกม 10 วินาทีแรกไม่นับ`() {
        val t = AutoTuner(GameSystem.PSP)
        assertNull(feed(t, 5, PerformanceSettings(), 15))
    }

    @Test
    fun `PSP ช้า ลดความละเอียดก่อน แล้วเร่งความเร็ว แล้วข้ามเฟรม`() {
        val t = AutoTuner(GameSystem.PSP)
        var s = PerformanceSettings(preset = PerformancePreset.BALANCED) // 2x
        s = feed(t, 15, s, 30)!!.settings
        assertEquals(1, s.resolutionScale)
        s = feed(t, 15, s, 30)!!.settings
        assertTrue(s.speedHacks)
        s = feed(t, 15, s, 30)!!.settings
        assertEquals(-1, s.frameSkip)
        assertNull(feed(t, 15, s, 30))
        assertTrue(t.exhausted)
    }

    @Test
    fun `ปิดปรับอัตโนมัติ ไม่ปรับให้`() {
        val t = AutoTuner(GameSystem.PS1)
        assertNull(feed(t, 10, PerformanceSettings(autoTune = false), 60))
    }
}
