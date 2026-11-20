# =============================================================================
# กฎของ R8 (ตัวย่อ/อำพรางโค้ดตอน build release)
# =============================================================================

# LibretroDroid เรียกคลาส Kotlin จากฝั่ง C++ (JNI) ด้วยชื่อ → ห้ามเปลี่ยนชื่อ/ลบ
-keep class com.swordfish.libretrodroid.** { *; }

# kotlinx.serialization: เก็บ serializer ของคลาสที่บันทึกเป็น JSON (ผังปุ่ม/ค่าตั้งค่า)
-keepclassmembers class com.allemulator.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.allemulator.**$$serializer { *; }
