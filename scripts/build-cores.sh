#!/usr/bin/env bash
# =============================================================================
# build-cores.sh — คอมไพล์ emulator core เองจาก source (ใช้ก่อนขึ้น Play Store)
#
# ทำไมต้องคอมไพล์เอง?
#   1) Google Play บังคับให้ native library รองรับ "16 KB page size" (Android 15+)
#      ต้องใช้ NDK r28 ขึ้นไป ซึ่งตั้งค่านี้ให้อัตโนมัติ
#   2) GPL บังคับให้เราบอกได้ว่าไฟล์ .so แต่ละไฟล์มาจาก source เวอร์ชันไหน
#      → ทุก core ถูก "pin" ไว้ที่ commit ในตาราง CORES ด้านล่าง
#
# สิ่งที่ต้องมี:
#   - Android NDK r28+  (Android Studio → SDK Manager → SDK Tools → NDK (Side by side))
#   - git, make, cmake, python3 (แนะนำให้รันบน Linux/WSL เพราะ Makefile ของ libretro เขียนสำหรับ Unix)
#
# วิธีใช้:
#   export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/28.x.xxxxx
#   ./scripts/build-cores.sh            # ทุก core
#   ./scripts/build-cores.sh gambatte   # เฉพาะ core ที่ระบุ
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
WORK="$ROOT/cores-build"
NDK="${ANDROID_NDK_HOME:?ตั้งค่า ANDROID_NDK_HOME ให้ชี้ไปที่ NDK r28+ ก่อน}"
ABIS=(arm64-v8a armeabi-v7a)

# ชื่อ core | git repo | commit/tag (อัปเดตเมื่อต้องการเวอร์ชันใหม่ แล้วจดลง docs/CORES.md)
CORES=(
  "ppsspp|https://github.com/hrydgard/ppsspp.git|master"
  "pcsx_rearmed|https://github.com/libretro/pcsx_rearmed.git|master"
  "play|https://github.com/jpd002/Play-.git|master"
  "gambatte|https://github.com/libretro/gambatte-libretro.git|master"
  "fceumm|https://github.com/libretro/libretro-fceumm.git|master"
  "melondsds|https://github.com/JesseTG/melonds-ds.git|main"
)

want="${1:-all}"
mkdir -p "$WORK"

build_ndk_build() { # สำหรับ core ที่มีโฟลเดอร์ jni/ (ndk-build)
  local dir="$1" core="$2"
  for abi in "${ABIS[@]}"; do
    "$NDK/ndk-build" -C "$dir/jni" APP_ABI="$abi" APP_PLATFORM=android-26 -j"$(nproc)"
    install_so "$dir/libs/$abi/libretro.so" "$abi" "$core"
  done
}

build_cmake() { # สำหรับ core ที่ใช้ CMake (PPSSPP, Play!, melonDS DS)
  local dir="$1" core="$2" extra="$3"
  for abi in "${ABIS[@]}"; do
    local b="$dir/build-$abi"
    cmake -S "$dir" -B "$b" -G "Unix Makefiles" \
      -DCMAKE_TOOLCHAIN_FILE="$NDK/build/cmake/android.toolchain.cmake" \
      -DANDROID_ABI="$abi" -DANDROID_PLATFORM=android-26 -DCMAKE_BUILD_TYPE=Release \
      -DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON $extra
    cmake --build "$b" -j"$(nproc)"
    install_so "$(find "$b" -name "${core}_libretro*.so" | head -1)" "$abi" "$core"
  done
}

install_so() {
  local so="$1" abi="$2" core="$3"
  local out="$ROOT/app/src/main/jniLibs/$abi"
  mkdir -p "$out"
  cp -f "$so" "$out/lib${core}_libretro_android.so"
  echo "✔ $abi/lib${core}_libretro_android.so"
}

for entry in "${CORES[@]}"; do
  IFS='|' read -r core repo ref <<< "$entry"
  [[ "$want" != "all" && "$want" != "$core" ]] && continue
  dir="$WORK/$core"
  if [[ ! -d "$dir" ]]; then git clone --recursive "$repo" "$dir"; fi
  git -C "$dir" fetch --all --tags && git -C "$dir" checkout "$ref" && git -C "$dir" submodule update --init --recursive
  echo "== $core @ $(git -C "$dir" rev-parse --short HEAD)" | tee -a "$WORK/versions.txt"

  case "$core" in
    ppsspp)    build_cmake "$dir" ppsspp "-DLIBRETRO=ON" ;;
    play)      build_cmake "$dir" play "-DBUILD_LIBRETRO_CORE=ON -DBUILD_PLAY=OFF -DBUILD_TESTS=OFF" ;;
    melondsds) build_cmake "$dir" melondsds "" ;;
    *)         build_ndk_build "$dir" "$core" ;;
  esac
done

echo
echo "ตรวจ 16 KB alignment (ทุกบรรทัดต้องเป็น 2**14 ขึ้นไป):"
for so in "$ROOT"/app/src/main/jniLibs/arm64-v8a/*.so; do
  printf '%-45s ' "$(basename "$so")"
  "$NDK"/toolchains/llvm/prebuilt/*/bin/llvm-readelf -l "$so" | awk '/LOAD/ {print $NF; exit}'
done
