# Emulator cores ที่ใช้

| เครื่อง | Core (ไฟล์ใน jniLibs) | License | Source |
|---|---|---|---|
| PSP | `libppsspp_libretro_android.so` | GPL-2.0+ | https://github.com/hrydgard/ppsspp |
| PS1 | `libpcsx_rearmed_libretro_android.so` | GPL-2.0 | https://github.com/libretro/pcsx_rearmed |
| GB / GBC | `libgambatte_libretro_android.so` | GPL-2.0 | https://github.com/libretro/gambatte-libretro |
| NES | `libfceumm_libretro_android.so` | GPL-2.0 | https://github.com/libretro/libretro-fceumm |
| GBA | `libmgba_libretro_android.so` | MPL-2.0 | https://github.com/libretro/mgba |
| (frontend) | LibretroDroid 0.14.0 | GPL-3.0 | https://github.com/Swordfish90/LibretroDroid |

**ทำไมเลือกตัวนี้:** ทุกตัวอนุญาตให้ใช้ในแอพที่มีรายได้ (core บางตัวเช่น Snes9x, PicoDrive ห้ามใช้เชิงพาณิชย์ จึงไม่เลือก)
และ DuckStation (PS1) เปลี่ยน license เป็น CC BY-NC-ND ในปี 2024 จึงใช้ PCSX-ReARMed แทน

## วิธีได้ไฟล์ core

| วิธี | คำสั่ง | ใช้เมื่อ |
|---|---|---|
| ดาวน์โหลดสำเร็จรูป | `./scripts/fetch-cores.sh` | พัฒนา/ทดสอบ |
| คอมไพล์เอง (NDK r28+) | `./scripts/build-cores.sh` | ก่อนปล่อยเวอร์ชันจริง (pin commit, ตรวจ 16 KB) |

ตรวจแล้ว (20 พ.ย. 2026): ไฟล์ arm64 จาก buildbot ผ่านเงื่อนไข 16 KB page size ทั้ง 6 ตัว

## พันธะของ GPL ที่ต้องทำ

1. เปิดซอร์สโค้ดของแอพทั้งหมด (เช่น GitHub) และใส่ลิงก์ในแอพ — ทำแล้วที่ หน้าตั้งค่า → เกี่ยวกับ
2. แสดงรายชื่อ license ของ core ในแอพ — ทำแล้วที่ หน้าตั้งค่า → License ของ emulator ที่ใช้
3. เก็บ commit ของ core ที่ใช้ build จริง (build-cores.sh เขียนลง `cores-build/versions.txt`) แล้วจดไว้ในไฟล์นี้ทุกครั้งที่ปล่อยเวอร์ชันใหม่
