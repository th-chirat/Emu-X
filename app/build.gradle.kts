// =============================================================================
// app/build.gradle.kts — โมดูลหลักที่ประกอบทุกโมดูลเข้าด้วยกันเป็นแอพ 1 ตัว
//
// ค่าที่ต้องแก้ก่อนขึ้น Play Store (ดู docs/RELEASE.md):
//   1) admobAppId / admobInterstitialId  → ใส่ใน gradle.properties ของเครื่องคุณ
//      (ไม่ใส่ = ใช้รหัสทดสอบของ Google อัตโนมัติ)
//   2) keystore.properties               → ข้อมูลกุญแจเซ็นแอพ (ห้ามอัปโหลดขึ้น GitHub)
//   3) versionCode / versionName         → เพิ่มทุกครั้งที่อัปโหลดเวอร์ชันใหม่
//   4) PRIVACY_URL (Google Sites) / SOURCE_URL (GitHub)
// =============================================================================
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// อ่านข้อมูลกุญแจเซ็นแอพ (ถ้ามี)
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// รหัส AdMob ทดสอบของ Google — รุ่น debug ใช้ตัวนี้เสมอ (กดโฆษณาจริงในแอพตัวเอง = AdMob มองว่าโกงคลิก)
val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testAdmobInterstitialId = "ca-app-pub-3940256099942544/1033173712"
// รหัสจริง (ตั้งใน gradle.properties) — ใช้กับ release; ไม่ได้ตั้ง = ใช้รหัสทดสอบ
val admobAppId = (findProperty("admobAppId") as String?) ?: testAdmobAppId
val admobInterstitialId = (findProperty("admobInterstitialId") as String?) ?: testAdmobInterstitialId

android {
    namespace = "com.allemulator.app"
    compileSdk = 36

    defaultConfig {
        // ชื่อแพ็กเกจบน Play Store — ต้องตรงกับที่ตั้งไว้ใน Play Console และ "ห้ามเปลี่ยน" หลังเผยแพร่
        // (namespace ด้านบนเป็นแค่ชื่อแพ็กเกจของโค้ด ไม่เกี่ยวกับ Play Store)
        applicationId = "com.chirat.emux"
        minSdk = 26 // Android 8.0 ตามบรีฟ
        targetSdk = 36
        versionCode = 3 // เพิ่มทุกครั้งที่อัปโหลดขึ้น Play Console
        versionName = "1.0.0"

        manifestPlaceholders["admobAppId"] = admobAppId
        manifestPlaceholders["admobInterstitialId"] = admobInterstitialId

        buildConfigField("String", "PRIVACY_URL", "\"https://sites.google.com/view/emu-x-privacy-policy\"")
        buildConfigField("String", "SOURCE_URL", "\"https://github.com/th-chirat/Emu-X\"")
    }

    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // ลงคู่กับเวอร์ชันจาก Play Store ได้ — ใส่ -PidSuffix=.demo ได้ลงอีกชุดที่ข้อมูลว่าง (ใช้ถ่ายภาพหน้าร้าน)
            applicationIdSuffix = (findProperty("idSuffix") as String?) ?: ".debug"
            manifestPlaceholders["admobAppId"] = testAdmobAppId
            manifestPlaceholders["admobInterstitialId"] = testAdmobInterstitialId
            // debug รวม x86_64 ด้วย เพื่อเทสบน Android Emulator ในคอม
            ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
            // มือถือจริงเกือบทั้งหมดเป็น ARM
            ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
        }
    }

    packaging {
        jniLibs {
            // แตกไฟล์ core (.so) ลงเครื่องตอนติดตั้ง เพื่อให้ LibretroDroid เปิดด้วย path ได้
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:emulation"))
    implementation(project(":feature:home"))
    implementation(project(":feature:library"))
    implementation(project(":feature:player"))
    implementation(project(":feature:settings"))
    implementation(project(":monetization"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    ksp(libs.hilt.compiler)
}
