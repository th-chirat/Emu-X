// =============================================================================
// build.gradle.kts (ราก) — ประกาศ plugin ที่ทุกโมดูลใช้ร่วมกัน (apply false = ยังไม่เปิดใช้)
// แต่ละโมดูลจะเลือกเปิด plugin ที่ตัวเองต้องการเอง
// =============================================================================
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
