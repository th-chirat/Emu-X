/*
 * VirtualButton.kt
 * -----------------------------------------------------------------------------
 * โมเดลของ "ปุ่มบนจอยเสมือน" หนึ่งปุ่ม
 *
 * ตำแหน่งเก็บเป็นสัดส่วน 0..1 ของหน้าจอ (ไม่ใช่ pixel) เพื่อให้ผังปุ่มเดียวกัน
 * ใช้ได้กับทุกขนาดจอ เช่น x = 0.1 หมายถึง 10% จากขอบซ้าย
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

import kotlinx.serialization.Serializable

/** ประเภทของปุ่ม — ใช้ตัดสินใจว่าจะวาดหน้าตาและส่ง input แบบไหน */
@Serializable
enum class ButtonKind {
    DPAD,       // ปุ่มทิศ 4 ทาง (ส่งเป็น motion event)
    ANALOG,     // อนาล็อกสติ๊ก (ส่งเป็น motion event)
    FACE,       // ปุ่มกลม A/B/X/Y, ○ × □ △
    SHOULDER,   // ปุ่มไหล่ L/R
    SMALL,      // ปุ่มเล็ก Start/Select
}

/**
 * ปุ่มหนึ่งปุ่ม
 *
 * @property id      รหัสปุ่ม (ไม่ซ้ำในผังเดียวกัน)
 * @property label   ตัวอักษรบนปุ่ม
 * @property keyCode รหัสปุ่มแบบ Android (KeyEvent.KEYCODE_BUTTON_*) ที่ LibretroDroid เข้าใจ
 *                   ใช้ 0 กับ DPAD/ANALOG เพราะสองแบบนี้ส่งเป็น motion event แทน
 * @property x       ตำแหน่งกึ่งกลางแนวนอน (0 = ซ้ายสุด, 1 = ขวาสุด)
 * @property y       ตำแหน่งกึ่งกลางแนวตั้ง (0 = บนสุด, 1 = ล่างสุด)
 * @property size    ขนาดปุ่ม เป็นสัดส่วนของ "ด้านสั้น" ของจอ
 * @property alpha   ความทึบ 0..1 (ผู้ใช้ปรับได้)
 * @property analogSource ใช้กับ ANALOG: 0 = สติ๊กซ้าย, 1 = สติ๊กขวา
 */
@Serializable
data class VirtualButton(
    val id: String,
    val label: String,
    val kind: ButtonKind,
    val keyCode: Int = 0,
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float = 0.75f,
    val analogSource: Int = 0,
)

/** ผังปุ่มทั้งชุดของเครื่องเกมหนึ่ง แยกแนวนอน/แนวตั้ง */
@Serializable
data class ControllerLayout(
    val landscape: List<VirtualButton>,
    val portrait: List<VirtualButton>,
) {
    /** เลือกผังตามทิศของจอ */
    fun forOrientation(isLandscape: Boolean) = if (isLandscape) landscape else portrait
}
