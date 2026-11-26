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
 *                         เกือบทุกเครื่องรับ .zip ได้ — แอพแตกไฟล์ให้อัตโนมัติก่อนเล่น
 *                         (ยกเว้น PS2: แผ่นใหญ่หลาย GB แตก zip ลงเครื่องไม่ไหว)
 * @property hasAnalog     true = เครื่องนี้มีอนาล็อก (PSP/PS1/PS2)
 * @property accentStart   สีเริ่มของการ์ด (ARGB)
 * @property accentEnd     สีปลายของการ์ด (ARGB) — ใช้ไล่สีพื้นหลังการ์ดกระจก
 * @property biosNoteTh    ข้อความแนะนำเรื่อง BIOS ภาษาไทย (null = ไม่ต้องใช้ BIOS) — ใช้ผ่าน biosNote
 * @property biosNoteEn    ข้อความเดียวกันภาษาอังกฤษ
 * @property biosDir       โฟลเดอร์ย่อยใต้ files/system ที่ core หา BIOS ("" = วางไว้ที่ system เลย)
 * @property biosRequired  true = ไม่มี BIOS เล่นไม่ได้ (PS2)
 * @property minSdk        Android ขั้นต่ำที่ core ทำงานได้
 * @property isBeta        true = ยังไม่นิ่ง → โชว์ป้าย BETA (PS2)
 * @property realCore      core จริงที่ [coreLibrary] โหลดต่ออีกที (PS2: coreLibrary เป็นตัวคั่นกลาง
 *                         native/armsx2-shim ที่แก้ปัญหา VFS ของ LibretroDroid) — null = ไม่มี
 */
enum class GameSystem(
    val id: String,
    val displayName: String,
    val fullName: String,
    val coreLibrary: String,
    val extensions: Set<String>,
    val hasAnalog: Boolean = false,
    val accentStart: Long,
    val accentEnd: Long,
    private val biosNoteTh: String? = null,
    private val biosNoteEn: String? = null,
    val biosDir: String = "",
    val biosRequired: Boolean = false,
    val minSdk: Int = 26,
    val realCore: String? = null,
    val isBeta: Boolean = false,
) {
    PSP(
        id = "psp",
        displayName = "PSP",
        fullName = "PlayStation Portable",
        coreLibrary = "libppsspp_libretro_android.so",
        extensions = setOf("iso", "cso", "pbp", "chd", "elf", "zip"),
        hasAnalog = true,
        accentStart = 0xFF3A7BFF, accentEnd = 0xFF00D4FF,
    ),
    PS1(
        id = "ps1",
        displayName = "PS1",
        fullName = "PlayStation",
        coreLibrary = "libpcsx_rearmed_libretro_android.so",
        extensions = setOf("cue", "bin", "img", "chd", "pbp", "m3u", "iso", "exe", "zip"),
        hasAnalog = true,
        accentStart = 0xFF6E6E8F, accentEnd = 0xFFC9C9E0,
        biosNoteTh = "ใช้ BIOS ในตัว (HLE) ได้เลย ถ้าเกมไหนมีปัญหาให้นำไฟล์ BIOS ของคุณเองมาวางในโฟลเดอร์ BIOS",
        biosNoteEn = "Works with the built-in BIOS (HLE). If a game has problems, import your own BIOS file.",
    ),
    PS2(
        id = "ps2",
        displayName = "PS2",
        fullName = "PlayStation 2",
        // ARMSX2 (PCSX2 ที่แปลงให้เร็วบนชิป ARM) — วาดภาพผ่าน OpenGL ES ของ LibretroDroid
        coreLibrary = "libarmsx2_shim.so",
        realCore = "libarmsx2_libretro_android.so",
        extensions = setOf("iso", "chd", "cso", "zso", "bin", "img", "mdf", "nrg", "gz", "elf", "m3u"),
        hasAnalog = true,
        accentStart = 0xFF1E2A78, accentEnd = 0xFF5B6CFF,
        biosNoteTh = "ต้องใช้ BIOS ของ PS2 ที่ดัมพ์จากเครื่องของคุณเอง: ตั้งค่า PS2 → นำเข้าไฟล์ BIOS (เลือกไฟล์ .bin หรือ .zip ที่มี BIOS อยู่ข้างใน)",
        biosNoteEn = "Needs a PS2 BIOS dumped from your own console: PS2 settings → Import BIOS file (pick the .bin, or a .zip that contains it).",
        biosDir = "pcsx2/bios",
        biosRequired = true,
        minSdk = 28, // core จาก buildbot build ด้วย Android API 28
        isBeta = true, // บรีฟรอบ 5 ข้อ 5: ยังไม่นิ่ง (core ARMSX2 เองก็ติดป้าย experimental)
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
    GBA(
        id = "gba",
        displayName = "GBA",
        fullName = "Game Boy Advance",
        coreLibrary = "libmgba_libretro_android.so",
        extensions = setOf("gba", "agb", "zip"),
        accentStart = 0xFF4B3BD6, accentEnd = 0xFF9E8CFF,
        biosNoteTh = "ไม่จำเป็นต้องมี BIOS (mGBA มี BIOS ทดแทนในตัว) ถ้ามีไฟล์ gba_bios.bin ของคุณเองใส่ในโฟลเดอร์ BIOS ได้",
        biosNoteEn = "No BIOS needed (mGBA has a built-in replacement). You can import your own gba_bios.bin.",
    ),
    NES(
        id = "nes",
        displayName = "NES",
        fullName = "Nintendo Entertainment System",
        coreLibrary = "libfceumm_libretro_android.so",
        extensions = setOf("nes", "fds", "unf", "unif", "zip"),
        accentStart = 0xFFE23B3B, accentEnd = 0xFFFF8A65,
    );

    /** ข้อความแนะนำเรื่อง BIOS ตามภาษาที่เลือก (null = ไม่ต้องใช้ BIOS) */
    val biosNote: String? get() = biosNoteTh?.let { tr(it, biosNoteEn ?: it) }

    /** true ถ้าชื่อไฟล์นี้มีนามสกุลที่เครื่องนี้รับ */
    fun accepts(fileName: String): Boolean =
        fileName.substringAfterLast('.', "").lowercase() in extensions

    companion object {
        /** หาเครื่องเกมจาก id ที่เก็บในฐานข้อมูล (ไม่เจอ = null) */
        fun fromId(id: String): GameSystem? = entries.firstOrNull { it.id == id }
    }
}
