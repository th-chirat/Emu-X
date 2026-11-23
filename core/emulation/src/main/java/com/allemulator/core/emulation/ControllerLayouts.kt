/*
 * ControllerLayouts.kt
 * -----------------------------------------------------------------------------
 * ผังปุ่ม "ค่าเริ่มต้น" ของแต่ละเครื่อง — ถ้าผู้ใช้กด "รีเซ็ต" ในหน้าแก้ไขปุ่ม
 * ระบบจะกลับมาใช้ค่าจากไฟล์นี้
 *
 * หลักคิดตำแหน่ง:
 *  - แนวนอน: ปุ่มทิศอยู่ซ้าย ปุ่มกดอยู่ขวา Start/Select อยู่ล่างกลาง
 *  - แนวตั้ง: จอเกมอยู่ครึ่งบน ปุ่มทั้งหมดอยู่ครึ่งล่าง
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

import android.view.KeyEvent

object ControllerLayouts {

    /** คืนผังปุ่มเริ่มต้นของเครื่องเกมที่ระบุ */
    fun defaultFor(system: GameSystem): ControllerLayout = when (system) {
        GameSystem.GB, GameSystem.GBC, GameSystem.NES -> twoButton()
        GameSystem.GBA -> gba()
        GameSystem.PSP -> playstation(twoShoulderRows = false, twoSticks = false)
        GameSystem.PS1 -> playstation(twoShoulderRows = true, twoSticks = true)
    }

    // ---------------- ตัวช่วยสร้างปุ่มแต่ละแบบ ----------------

    private fun dpad(x: Float, y: Float, s: Float) =
        VirtualButton("dpad", "", ButtonKind.DPAD, x = x, y = y, size = s)

    private fun face(id: String, label: String, key: Int, x: Float, y: Float, s: Float) =
        VirtualButton(id, label, ButtonKind.FACE, key, x, y, s)

    private fun small(id: String, label: String, key: Int, x: Float, y: Float) =
        VirtualButton(id, label, ButtonKind.SMALL, key, x, y, 0.11f)

    private fun shoulder(id: String, label: String, key: Int, x: Float, y: Float) =
        VirtualButton(id, label, ButtonKind.SHOULDER, key, x, y, 0.16f)

    private fun stick(id: String, source: Int, x: Float, y: Float) =
        VirtualButton(id, "", ButtonKind.ANALOG, x = x, y = y, size = 0.28f, analogSource = source)

    private const val START = KeyEvent.KEYCODE_BUTTON_START
    private const val SELECT = KeyEvent.KEYCODE_BUTTON_SELECT

    // ---------------- เครื่อง 2 ปุ่ม (GB / GBC / NES) ----------------
    private fun twoButton() = ControllerLayout(
        landscape = listOf(
            dpad(0.12f, 0.68f, 0.38f),
            face("b", "B", KeyEvent.KEYCODE_BUTTON_B, 0.80f, 0.76f, 0.17f),
            face("a", "A", KeyEvent.KEYCODE_BUTTON_A, 0.91f, 0.60f, 0.17f),
            small("select", "SELECT", SELECT, 0.42f, 0.93f),
            small("start", "START", START, 0.58f, 0.93f),
        ),
        portrait = listOf(
            dpad(0.25f, 0.74f, 0.40f),
            face("b", "B", KeyEvent.KEYCODE_BUTTON_B, 0.68f, 0.78f, 0.18f),
            face("a", "A", KeyEvent.KEYCODE_BUTTON_A, 0.85f, 0.70f, 0.18f),
            small("select", "SELECT", SELECT, 0.38f, 0.94f),
            small("start", "START", START, 0.62f, 0.94f),
        ),
    )

    // ---------------- GBA (2 ปุ่ม + L/R) ----------------
    private fun gba() = ControllerLayout(
        landscape = twoButton().landscape + listOf(
            shoulder("l", "L", KeyEvent.KEYCODE_BUTTON_L1, 0.08f, 0.12f),
            shoulder("r", "R", KeyEvent.KEYCODE_BUTTON_R1, 0.92f, 0.12f),
        ),
        portrait = twoButton().portrait + listOf(
            shoulder("l", "L", KeyEvent.KEYCODE_BUTTON_L1, 0.12f, 0.56f),
            shoulder("r", "R", KeyEvent.KEYCODE_BUTTON_R1, 0.88f, 0.56f),
        ),
    )

    // ---------------- ตระกูล PlayStation (PSP / PS1) ----------------
    // libretro ใช้ตำแหน่งปุ่มแบบ SNES: A = ขวา(○)  B = ล่าง(×)  X = บน(△)  Y = ซ้าย(□)
    private fun playstation(twoShoulderRows: Boolean, twoSticks: Boolean): ControllerLayout {
        fun faces(cx: Float, cy: Float, gapX: Float, gapY: Float, s: Float) = listOf(
            face("square", "□", KeyEvent.KEYCODE_BUTTON_Y, cx - gapX, cy, s),
            face("triangle", "△", KeyEvent.KEYCODE_BUTTON_X, cx, cy - gapY, s),
            face("cross", "×", KeyEvent.KEYCODE_BUTTON_B, cx, cy + gapY, s),
            face("circle", "○", KeyEvent.KEYCODE_BUTTON_A, cx + gapX, cy, s),
        )

        fun shoulders(top: Float, rowGap: Float) = buildList {
            add(shoulder("l1", "L1", KeyEvent.KEYCODE_BUTTON_L1, 0.09f, top))
            add(shoulder("r1", "R1", KeyEvent.KEYCODE_BUTTON_R1, 0.91f, top))
            if (twoShoulderRows) {
                add(shoulder("l2", "L2", KeyEvent.KEYCODE_BUTTON_L2, 0.09f, top - rowGap))
                add(shoulder("r2", "R2", KeyEvent.KEYCODE_BUTTON_R2, 0.91f, top - rowGap))
            }
        }

        val landscape = buildList {
            add(dpad(0.11f, 0.50f, 0.30f))
            addAll(faces(0.89f, 0.50f, 0.065f, 0.12f, 0.12f))
            addAll(shoulders(top = 0.20f, rowGap = 0.11f))
            add(stick("lstick", 0, 0.27f, 0.80f))
            if (twoSticks) add(stick("rstick", 1, 0.73f, 0.80f))
            add(small("select", "SELECT", SELECT, 0.42f, 0.94f))
            add(small("start", "START", START, 0.58f, 0.94f))
        }
        val portrait = buildList {
            add(dpad(0.22f, 0.68f, 0.30f))
            addAll(faces(0.78f, 0.68f, 0.11f, 0.065f, 0.14f))
            addAll(shoulders(top = 0.58f, rowGap = 0.05f))
            add(stick("lstick", 0, 0.30f, 0.87f))
            if (twoSticks) add(stick("rstick", 1, 0.70f, 0.87f))
            add(small("select", "SELECT", SELECT, 0.40f, 0.975f))
            add(small("start", "START", START, 0.60f, 0.975f))
        }
        return ControllerLayout(landscape, portrait)
    }
}
