# Emu-X

แอพ Android รวม emulator 6 เครื่องในแอพเดียว — **PSP, PS1, Game Boy, Game Boy Color, Game Boy Advance, NES**
UI สไตล์ **Liquid Glass** · รองรับ **Android 8.0 (API 26) ขึ้นไป** · มีโฆษณาทุก 1 ชม. + ซื้อลบโฆษณาตลอดชีพ

> แอพนี้ **ไม่มีเกมหรือ BIOS แถมมา** ผู้ใช้ต้องใช้ไฟล์เกมที่ตัวเองเป็นเจ้าของโดยชอบด้วยกฎหมาย
> ซอร์สโค้ดเปิดเผยภายใต้ **GPL-3.0** (เพราะใช้ emulator core ที่เป็น GPL)

---

## เริ่มต้นใช้งาน (สำหรับนักพัฒนา)

```bash
# 1) ดาวน์โหลด emulator core (ครั้งแรกครั้งเดียว ~150 MB)
./scripts/fetch-cores.sh

# 2) build + ติดตั้งลงมือถือ/emulator ที่ต่ออยู่
./gradlew installDebug

# 3) รัน unit test
./gradlew testDebugUnitTest
```

หรือเปิดโฟลเดอร์นี้ใน **Android Studio** แล้วกด ▶ Run ได้เลย (หลังทำข้อ 1)

| ต้องมี | เวอร์ชัน |
|---|---|
| JDK | 17 |
| Android SDK | API 36 |
| Android Studio | Narwhal ขึ้นไป (AGP 8.13) |
| NDK r28+ | เฉพาะตอน build core เอง (`scripts/build-cores.sh`) |

---

## โครงสร้างโปรเจกต์ (อ่านละเอียดใน [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md))

```
app/                  ประกอบทุกโมดูล + navigation + MainActivity
core/designsystem/    ชุด UI Liquid Glass (กระจก, ปุ่ม, การ์ด, ไอคอนเครื่องเกม)
core/data/            ฐานข้อมูลเกม (Room) + ค่าตั้งค่า (DataStore) + สแกนโฟลเดอร์
core/emulation/       ข้อมูลเครื่องเกม, ผังปุ่ม, preset ความลื่น → core options
feature/home/         หน้าแรก (การ์ดเครื่องเกม)
feature/library/      หน้ารายการเกม + ปุ่ม + เลือกโฟลเดอร์
feature/player/       หน้าเล่นเกม + จอยเสมือน + overlay + แก้ไขผังปุ่ม
feature/settings/     ตั้งค่าแต่ละเครื่อง + ตั้งค่าแอพ + ซื้อลบโฆษณา
monetization/         AdMob (ทุก 1 ชม.) + Play Billing
scripts/              ดาวน์โหลด/คอมไพล์ core, สร้าง graph
docs/                 เอกสารทั้งหมด
graphify-out/         แผนที่โค้ด (knowledge graph) — เปิด graph.html ในเบราว์เซอร์
```

## เอกสาร

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — โค้ดทำงานยังไง ไฟล์ไหนทำอะไร (มีแผนภาพ)
- [docs/RELEASE.md](docs/RELEASE.md) — ขั้นตอนขึ้น Google Play ทีละขั้น
- [docs/CORES.md](docs/CORES.md) — emulator core ที่ใช้ + license + วิธีอัปเดต
- [docs/PRIVACY_POLICY.md](docs/PRIVACY_POLICY.md) — นโยบายความเป็นส่วนตัว (ต้องใช้ตอนขึ้น Play Store)
- `graphify-out/GRAPH_REPORT.md` + `graphify-out/graph.html` — แผนที่ความสัมพันธ์ของโค้ด

## License

GPL-3.0 — ดูไฟล์ [LICENSE](LICENSE) และ license ของแต่ละ core ใน [docs/CORES.md](docs/CORES.md)
