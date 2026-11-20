/*
 * GameSystem.kt
 * -----------------------------------------------------------------------------
 * "ทะเบียนเครื่องเกม" — ข้อมูลประจำตัวของเครื่องเกมทั้ง 7 เครื่องอยู่ที่นี่ที่เดียว
 *
 * อยากเพิ่มเครื่องเกมใหม่ในอนาคต? ทำ 3 อย่าง:
 *   1) เพิ่มค่าใน enum นี้
 *   2) เพิ่มชื่อ core ใน scripts/fetch-cores.sh (และ build-cores.sh)
 *   3) เพิ่มผังปุ่มใน ControllerLayouts.kt และ preset ใน CoreOptions.kt
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.emulation

/**
 * เครื่องเกมหนึ่งเครื่อง
 *
 * @property id            รหัสภายใน (ใช้เก็บลงฐานข้อมูล ห้ามเปลี่ยนหลังปล่อยแอพ)
 * @property displayName   ชื่อสั้นที่โชว์บนการ์ด
 * @property fullName      ชื่อเต็มที่โชว์ในหน้ารายการเกม
 * @property coreLibrary   ชื่อไฟล์ .so ของ libretro core (อยู่ใน app/src/main/jniLibs/<abi>/)
 * @property extensions    นามสกุลไฟล์เกมที่เครื่องนี้รับ (ตัวเล็กทั้งหมด ไม่มีจุด)
 * @property isBeta        true = ยังไม่เสถียร (PS2) → โชว์ป้าย Beta + คำเตือน
 * @property dualScreen    true = มี 2 จอ (NDS)
 * @property hasAnalog     true = เครื่องนี้มีอนาล็อก (PSP/PS1/PS2)
 * @property accentStart   สีเริ่มของการ์ด (ARGB)
 * @property accentEnd     สีปลายของการ์ด (ARGB) — ใช้ไล่สีพื้นหลังการ์ดกระจก
 * @property biosNote      ข้อความแนะนำเรื่อง BIOS (null = ไม่ต้องใช้ BIOS)
 */
enum class GameSystem(
    val id: String,
    val displayName: String,
    val fullName: String,
    val coreLibrary: String,
    val extensions: Set<String>,
    val isBeta: Boolean = false,
    val dualScreen: Boolean = false,
    val hasAnalog: Boolean = false,
    val accentStart: Long,
    val accentEnd: Long,
    val biosNote: String? = null,
) {
    PSP(
        id = "psp",
        displayName = "PSP",
        fullName = "PlayStation Portable",
        coreLibrary = "libppsspp_libretro_android.so",
        extensions = setOf("iso", "cso", "pbp", "chd", "elf"),
        hasAnalog = true,
        accentStart = 0xFF3A7BFF, accentEnd = 0xFF00D4FF,
    ),
    PS1(
        id = "ps1",
        displayName = "PS1",
        fullName = "PlayStation",
        coreLibrary = "libpcsx_rearmed_libretro_android.so",
        extensions = setOf("cue", "bin", "img", "chd", "pbp", "m3u", "iso", "exe"),
        hasAnalog = true,
        accentStart = 0xFF6E6E8F, accentEnd = 0xFFC9C9E0,
        biosNote = "ใช้ BIOS ในตัว (HLE) ได้เลย ถ้าเกมไหนมีปัญหาให้นำไฟล์ BIOS ของคุณเองมาวางในโฟลเดอร์ BIOS",
    ),
    PS2(
        id = "ps2",
        displayName = "PS2",
        fullName = "PlayStation 2",
        coreLibrary = "libplay_libretro_android.so",
        extensions = setOf("iso", "chd", "cso", "isz", "bin", "cue", "elf"),
        isBeta = true,
        hasAnalog = true,
        accentStart = 0xFF1E2A78, accentEnd = 0xFF5B6CFF,
    ),
    GB(
        id = "gb",
        displayName = "Game Boy",
        fullName = "Game Boy",
        coreLibrary = "libgambatte_libretro_android.so",
        extensions = setOf("gb", "zip"),
        accentStart = 0xFF5E8A3A, accentEnd = 0xFFC4DF8E,
    ),
    GBC(
        id = "gbc",
        displayName = "GB Color",
        fullName = "Game Boy Color",
        coreLibrary = "libgambatte_libretro_android.so",
        extensions = setOf("gbc", "zip"),
        accentStart = 0xFFB040FF, accentEnd = 0xFFFF6AD5,
    ),
    NES(
        id = "nes",
        displayName = "NES",
        fullName = "Nintendo Entertainment System",
        coreLibrary = "libfceumm_libretro_android.so",
        extensions = setOf("nes", "fds", "unf", "unif", "zip"),
        accentStart = 0xFFE23B3B, accentEnd = 0xFFFF8A65,
    ),
    NDS(
        id = "nds",
        displayName = "NDS",
        fullName = "Nintendo DS",
        coreLibrary = "libmelondsds_libretro_android.so",
        extensions = setOf("nds", "dsi", "ids", "zip"),
        dualScreen = true,
        accentStart = 0xFF00B3A4, accentEnd = 0xFF7CF5E6,
        biosNote = "ไม่จำเป็นต้องมี BIOS (มี FreeBIOS ในตัว) แต่ถ้าใส่ BIOS/firmware ของคุณเอง ความเข้ากันได้จะดีขึ้น",
    );

    /** true ถ้าชื่อไฟล์นี้มีนามสกุลที่เครื่องนี้รับ */
    fun accepts(fileName: String): Boolean =
        fileName.substringAfterLast('.', "").lowercase() in extensions

    companion object {
        /** หาเครื่องเกมจาก id ที่เก็บในฐานข้อมูล (ไม่เจอ = null) */
        fun fromId(id: String): GameSystem? = entries.firstOrNull { it.id == id }
    }
}
