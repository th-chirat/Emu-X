/*
 * CleanTitleTest.kt — ทดสอบการทำชื่อไฟล์เกมให้อ่านง่าย
 */
package com.allemulator.core.data.scan

import org.junit.Assert.assertEquals
import org.junit.Test

class CleanTitleTest {
    @Test
    fun removes_region_tags_and_extension() {
        assertEquals("Super Game", GameScanner.cleanTitle("Super Game (USA) [!].nes"))
    }

    @Test
    fun underscores_become_spaces() {
        assertEquals("my homebrew", GameScanner.cleanTitle("my_homebrew.gb"))
    }

    @Test
    fun keeps_name_when_everything_is_a_tag() {
        assertEquals("(Demo)", GameScanner.cleanTitle("(Demo).nds"))
    }
}
