// =============================================================================
// monetization — โฆษณา AdMob ทุก 3 ชม. + ซื้อลบโฆษณาตลอดชีพ (Play Billing)
// =============================================================================
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.allemulator.monetization"
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
    implementation(project(":core:data"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.billing)
    implementation(libs.play.services.ads)
    implementation(libs.ump)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.process)
}
