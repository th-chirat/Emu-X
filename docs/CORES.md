# Emulator cores ที่ใช้

| เครื่อง | Core (ไฟล์ใน jniLibs) | License | Source |
|---|---|---|---|
| PSP | `libppsspp_libretro_android.so` | GPL-2.0+ | https://github.com/hrydgard/ppsspp |
| PS1 | `libpcsx_rearmed_libretro_android.so` | GPL-2.0 | https://github.com/libretro/pcsx_rearmed |
| PS2 | `libarmsx2_libretro_android.so` (โหลดผ่าน `libarmsx2_shim.so`) | GPL-3.0 | https://github.com/ARMSX2/ARMSX2 |
| GB / GBC | `libgambatte_libretro_android.so` | GPL-2.0 | https://github.com/libretro/gambatte-libretro |
| NES | `libfceumm_libretro_android.so` | GPL-2.0 | https://github.com/libretro/libretro-fceumm |
| GBA | `libmgba_libretro_android.so` | MPL-2.0 | https://github.com/libretro/mgba |
| (frontend) | LibretroDroid 0.14.0 | GPL-3.0 | https://github.com/Swordfish90/LibretroDroid |

**ทำไมเลือกตัวนี้:** ทุกตัวอนุญาตให้ใช้ในแอพที่มีรายได้ (core บางตัวเช่น Snes9x, PicoDrive ห้ามใช้เชิงพาณิชย์ จึงไม่เลือก)
และ DuckStation (PS1) เปลี่ยน license เป็น CC BY-NC-ND ในปี 2024 จึงใช้ PCSX-ReARMed แทน

## PS2 (ARMSX2) — หมายเหตุสำคัญ

- core ARMSX2 (PCSX2 ที่แปลงให้เร็วบนชิป ARM) มีแค่ arm64 และต้องใช้ Android 9+ (build ด้วย API 28)
- วาดภาพด้วย OpenGL ES 3.2 ผ่าน LibretroDroid (ตั้ง `armsx2_renderer=OpenGL` ใน CoreOptions — ค่าเริ่มต้นของ core คือ Vulkan ซึ่ง LibretroDroid ไม่มี)
- **ต้องมีตัวคั่นกลาง `native/armsx2-shim`**: LibretroDroid 0.14.0 ตอบว่ามี VFS เวอร์ชัน 3 ทั้งที่มีแค่ 2
  → core เด้งตอนเปิดเกม ตัวคั่นกลางให้ VFS เวอร์ชัน 3 ที่ครบ (stat/mkdir/โฟลเดอร์ทำเอง) แทน
  fetch-cores.sh คอมไพล์ให้ (ต้องตั้ง `ANDROID_NDK_HOME`)
- ต้องใช้ BIOS ของผู้ใช้เอง: วางที่ `files/system/pcsx2/bios/` (ตั้งค่า PS2 → นำเข้าไฟล์ BIOS, รับ .zip)
- ทดสอบแล้ว (OPPO Reno16, Adreno 722): Downhill Domination และ God of War เล่นได้ 59–60 FPS, CPU ~20%

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
