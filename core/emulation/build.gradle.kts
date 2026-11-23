// =============================================================================
// core/emulation — ข้อมูลเครื่องเกมแต่ละตัว (core, นามสกุลไฟล์, ปุ่ม, preset ความลื่น)
// =============================================================================
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.allemulator.core.emulation"
    compileSdk = 36

    defaultConfig {
        minSdk = 26 // Android 8.0 ตามบรีฟ
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.android) // StateFlow ของภาษา (L10n.kt)
    testImplementation(libs.junit)
}
