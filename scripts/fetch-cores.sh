#!/usr/bin/env bash
# =============================================================================
# fetch-cores.sh — ดาวน์โหลด emulator core สำเร็จรูปจาก libretro buildbot (ทางการ)
#
# ใช้สำหรับ "พัฒนา/ทดสอบ" ให้เร็ว
# ก่อนขึ้น Play Store ให้ใช้ build-cores.sh แทน (คอมไพล์เองจาก commit ที่ระบุ
# เพื่อให้รองรับ 16 KB page size ตามที่ Google บังคับ และอ้างอิง source ตาม GPL ได้)
#
# วิธีใช้ (Git Bash / Linux / macOS):
#   ./scripts/fetch-cores.sh                 # ทุก ABI (arm64-v8a armeabi-v7a x86_64)
#   ./scripts/fetch-cores.sh arm64-v8a       # เฉพาะ ABI ที่ระบุ
#
# ผลลัพธ์:
#   app/src/main/jniLibs/<abi>/lib<core>_libretro_android.so
#   app/src/main/assets/system/PPSSPP/        (ไฟล์ระบบที่ PSP core ต้องใช้)
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILDBOT="https://buildbot.libretro.com/nightly/android/latest"
ABIS=("${@:-arm64-v8a armeabi-v7a x86_64}")
# ถ้าไม่ส่ง argument มา ABIS จะเป็น string เดียว → แยกเป็น array
read -r -a ABIS <<< "${ABIS[*]}"

# core ที่แอพใช้ (ต้องตรงกับ GameSystem.coreLibrary)
CORES=(ppsspp pcsx_rearmed play gambatte fceumm melondsds)

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

for abi in "${ABIS[@]}"; do
  out="$ROOT/app/src/main/jniLibs/$abi"
  mkdir -p "$out"
  for core in "${CORES[@]}"; do
    name="${core}_libretro_android.so"
    echo "→ $abi / $name"
    if curl -fsSL "$BUILDBOT/$abi/$name.zip" -o "$TMP/$name.zip"; then
      unzip -o -q "$TMP/$name.zip" -d "$TMP/$abi"
      # Android บังคับให้ไฟล์ใน jniLibs ขึ้นต้นด้วย "lib"
      mv -f "$TMP/$abi/$name" "$out/lib$name"
    else
      echo "   ! ไม่มี $name สำหรับ $abi บน buildbot (ข้าม)"
    fi
  done
done

# ---- ไฟล์ระบบของ PPSSPP (ฟอนต์, flash0, ฐานข้อมูลเกม) ----
echo "→ PPSSPP system assets"
curl -fsSL "https://buildbot.libretro.com/assets/system/PPSSPP.zip" -o "$TMP/PPSSPP.zip"
rm -rf "$ROOT/app/src/main/assets/system/PPSSPP"
mkdir -p "$ROOT/app/src/main/assets/system"
unzip -o -q "$TMP/PPSSPP.zip" -d "$ROOT/app/src/main/assets/system"

echo "เสร็จแล้ว ✔  ไฟล์ทั้งหมด:"
find "$ROOT/app/src/main/jniLibs" -name "*.so" | while read -r f; do du -h "$f"; done
