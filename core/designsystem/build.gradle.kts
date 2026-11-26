// =============================================================================
// core/designsystem — ชุด UI สไตล์ Liquid Glass ที่ทุกหน้าใช้ร่วมกัน
// =============================================================================
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.allemulator.core.designsystem"
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
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    api(libs.haze)
    implementation(libs.androidx.core.ktx) // WindowCompat: สีไอคอนแถบสถานะตามธีม
    implementation(project(":core:emulation"))
}
