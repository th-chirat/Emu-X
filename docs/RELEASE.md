# ขั้นตอนขึ้น Google Play Store

เช็คลิสต์ทีละขั้น — ขั้นที่มี 👤 ต้องทำเองในเว็บ (ใช้บัญชีของคุณ)

## A. เตรียมบัญชี (ครั้งเดียว)

1. 👤 สมัคร **Google Play Console** — https://play.google.com/console (ค่าธรรมเนียม $25 ครั้งเดียว) และยืนยันตัวตน
2. 👤 สมัคร **AdMob** — https://admob.google.com → Apps → Add app (Android)
   - จดรหัส **App ID** (`ca-app-pub-xxxx~yyyy`)
   - Ad units → Add → **Interstitial** → จดรหัส **Ad unit ID** (`ca-app-pub-xxxx/zzzz`)
3. 👤 ใส่ข้อมูลรับเงิน/ภาษีใน Play Console (Payments profile) และ AdMob

## B. ตั้งค่าในโปรเจกต์

1. ใส่รหัส AdMob จริงในไฟล์ `~/.gradle/gradle.properties` (ไฟล์ส่วนตัว ไม่ขึ้น GitHub):
   ```properties
   admobAppId=ca-app-pub-xxxx~yyyy
   admobInterstitialId=ca-app-pub-xxxx/zzzz
   ```
   > ⚠️ ห้ามกดโฆษณาจริงของตัวเองเด็ดขาด บัญชี AdMob จะโดนแบน — ตอนเทสให้ใช้ debug build (ใช้รหัสทดสอบอัตโนมัติ)
2. แก้ลิงก์ `PRIVACY_URL` / `SOURCE_URL` ใน `app/build.gradle.kts` ให้เป็น GitHub ของคุณ
3. สร้างกุญแจเซ็นแอพ (ครั้งเดียว — **เก็บไฟล์นี้และรหัสให้ดี หายแล้วอัปเดตแอพไม่ได้**):
   ```bash
   keytool -genkeypair -v -keystore all-emulator-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```
   แล้วสร้างไฟล์ `keystore.properties` ที่รากโปรเจกต์ (ไฟล์นี้อยู่ใน .gitignore แล้ว):
   ```properties
   storeFile=all-emulator-upload.jks
   storePassword=รหัสของคุณ
   keyAlias=upload
   keyPassword=รหัสของคุณ
   ```
4. คอมไพล์ core เองด้วย NDK r28+ (แนะนำ) — `./scripts/build-cores.sh` แล้วจด commit ลง `docs/CORES.md`
5. เพิ่ม `versionCode` (+1 ทุกครั้งที่อัปโหลด) และ `versionName` ใน `app/build.gradle.kts`
6. Build ไฟล์สำหรับอัปโหลด:
   ```bash
   ./gradlew bundleRelease
   ```
   ได้ไฟล์ `app/build/outputs/bundle/release/app-release.aab`

## C. สร้างแอพใน Play Console 👤

1. Create app → ชื่อ **Emu-X** → App (ไม่ใช่ Game ก็ได้ แต่แนะนำ **Game**) → Free
2. **App content** (ต้องกรอกครบก่อนปล่อย):
   - Privacy policy → ลิงก์ไปที่ `docs/PRIVACY_POLICY.md` บน GitHub
   - Ads → **Yes, my app contains ads**
   - Data safety → เก็บ: Device or other IDs (โฆษณา AdMob), Purchase history (Play Billing) · ไม่ขาย/ไม่แชร์ข้อมูลอื่น
   - Content rating → ทำแบบสอบถาม (แอพ emulator ทั่วไปได้ระดับ Everyone/3+)
   - Target audience → **18+ หรือ 13+** (อย่าเลือกเด็กเพราะมีโฆษณาแบบปรับแต่ง)
3. **Monetize → Products → In-app products** → Create product
   - Product ID: **`remove_ads`** (ต้องตรงกับโค้ด) · ชื่อ "ลบโฆษณาตลอดชีพ" · ตั้งราคา → **Activate**
4. **Store listing**
   - ⚠️ **ห้าม** ใช้โลโก้/ภาพเกมของ Sony, Nintendo หรือชื่อเกมจริงในภาพหน้าจอ
   - ห้ามเขียนว่ามีเกมให้เล่น — เขียนว่า "ใช้กับไฟล์เกมที่คุณเป็นเจ้าของ"
   - ภาพหน้าจอ: ใช้หน้าแรก/หน้าตั้งค่า/หน้าเล่นเกมด้วย homebrew เท่านั้น
5. **Testing**
   - Internal testing → อัปโหลด `.aab` → เพิ่มอีเมลตัวเองเป็น tester → ทดสอบซื้อ (เพิ่มอีเมลเป็น **License tester** ที่ Settings → License testing จะซื้อได้โดยไม่เสียเงินจริง)
   - ⚠️ **บัญชีนักพัฒนาส่วนตัวที่สมัครใหม่** ต้องทำ **Closed testing ให้มี tester อย่างน้อย 12 คน ต่อเนื่อง 14 วัน** ก่อนจึงจะขอขึ้น Production ได้
6. Production → Create release → อัปโหลด `.aab` → ส่งรีวิว (ปกติ 1–7 วัน)

## D. หลังปล่อยแอพ

- ดู crash ได้ที่ Play Console → Quality → Android vitals
- ทุกครั้งที่อัปเดต: เพิ่ม `versionCode` → `./gradlew bundleRelease` → อัปโหลด
- อัปเดต source บน GitHub ให้ตรงกับเวอร์ชันที่ปล่อย (พันธะ GPL)
