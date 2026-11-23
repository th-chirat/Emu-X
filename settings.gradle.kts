// =============================================================================
// settings.gradle.kts — ไฟล์บอก Gradle ว่าโปรเจกต์นี้มีโมดูลอะไรบ้าง
// และจะไปดาวน์โหลด library จาก repository ไหน
// =============================================================================
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // JitPack ใช้สำหรับ LibretroDroid (ตัวกลางเชื่อมแอพกับ emulator core)
        maven("https://jitpack.io") {
            content { includeGroup("com.github.Swordfish90") }
        }
    }
}

rootProject.name = "Emu-X"

// ---- โมดูลทั้งหมดของแอพ (ดูแผนภาพใน docs/ARCHITECTURE.md) ----
include(":app")                     // จุดเริ่มแอพ + navigation
include(":core:designsystem")       // ชุด UI แบบ Liquid Glass
include(":core:data")               // ฐานข้อมูลเกม + ค่าตั้งค่า
include(":core:emulation")          // ข้อมูลเครื่องเกม + ตัวเชื่อม emulator core
include(":feature:home")            // หน้าแรก (การ์ดเครื่องเกม)
include(":feature:library")         // หน้ารายการเกม + เพิ่มโฟลเดอร์
include(":feature:player")          // หน้าเล่นเกม + จอยเสมือน + overlay
include(":feature:settings")        // หน้าตั้งค่า
include(":monetization")            // โฆษณา + ซื้อลบโฆษณา
