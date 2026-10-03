# 🛡️ APK Himoyachi — Android Xavfsizlik va Maxfiylik Ilovasi

**APK Himoyachi** — Android qurilmalari uchun mo'ljallangan zamonaviy, tezkor va eng muhimi **100% maxfiy (oflayn)** xavfsizlik dasturi.

Ilova qurilmadagi o'rnatilgan dasturlar, xotiradagi `.apk` o'rnatish paketlari hamda noma'lum manbalardan yuklangan dasturlarni chuqur tahlil qilib, viruslar, troyanlar, josuslik dasturlari (spyware) va shubhali ruxsatlarni aniqlaydi.

---

## 🔒 1. 100% Maxfiylik Kafolati (Zero-Telemetry)

Aksariyat antiviruslar foydalanuvchining ma'lumotlarini o'z serverlariga yuklaydi. **APK Himoyachi** esa quyidagi qat'iy tamoyillarga asoslangan:
- **Internet ruxsati yo'q:** `AndroidManifest.xml` faylida `android.permission.INTERNET` ruxsati umuman mavjud emas. Bu degani dastur hech qanday texnik imkoniyat bilan ma'lumotlarni tashqariga chiqara olmaydi.
- **Lokal evristik tahlil (On-Device Heuristics):** Barcha tahlillar faqat qurilmaning o'zida amalga oshiriladi.
- **Shaxsiy ma'lumotlar to'planmaydi:** Foydalanuvchining kontaktlari, fotosuratlari, xabarlari yoki fayllari hech qachon yozib olinmaydi.

---

## ⚡ 2. Asosiy Imkoniyatlar

### 📱 A. O'rnatilgan dasturlar tahlili (App Analyzer)
- Har bir ilovaning xavflilik ballini (0 dan 100 gacha) va darajasini (`Xavfsiz`, `Past`, `O'rtacha`, `Yuqori`, `Kritik`) hisoblaydi.
- Ilovalarning ruxsatnomalarini tahlil qiladi (Mikrofon, Kamera, SMS, Kontaktlar, Joylashuv).
- **Zararli kombinatsiyalarni aniqlash:**
  - `Accessibility + Overlay`: Bank troyanlari andozasi (parol va kartalarni o'g'irlash).
  - `SMS + Boot Completed`: Kiruvchi bank tasdiqlash kodlarini (OTP) yashirin ushlab qolish.
  - `Install Packages + Boot Completed`: Dropper (boshqa viruslarni yashirin o'rnatuvchi) xususiyati.
  - `Audio + Camera + Background Location`: Stalkerware / Josuslik xususiyati.

### 🌐 B. Noma'lum manbalarni aniqlash (Sideload Detection)
- Ilovalarning qayerdan o'rnatilganini (`InstallSourceInfo`) tekshiradi.
- Rasmiy do'kondan (Google Play) emas, balki Telegram, brauzer yoki shubhali manbalardan o'rnatilgan ilovalarni darhol nishonlaydi.

### 📁 C. Xotiradagi APK fayllarni skanerlash (Storage APK Scanner)
- `Downloads` va saqlash xotirasidagi barcha o'rnatilmagan `.apk` fayllarni topadi.
- Buzilgan yoki himoyani chalg'itishga uringan fayllarni aniqlaydi.
- Soxta tizim ilovalarini (Spoofing — masalan, o'zini Google xizmatlari deb ko'rsatuvchi viruslar) fosh qiladi.
- Xavfli APK fayllarni to'g'ridan-to'g'ri ilovaning o'zidan bir bosishda o'chirib tashlash imkoniyati.

### ⏱️ D. Real vaqt rejimida himoya (Real-time Protection)
- **Yangi ilova o'rnatilganda (`PackageInstallReceiver`):** Yangi ilova paydo bo'lishi bilan soniyalar ichida fon rejimida tahlil qilinadi va agar xavfli bo'lsa, foydalanuvchiga darhol yuqori darajadagi ogohlantirish (Notification) yuboriladi.
- **APK fayl yuklanganda (`RealtimeProtectionService`):** `FileObserver` orqali yuklab olingan har qanday yangi `.apk` fayl o'rnatilishidan avval tahlil qilinadi.

---

## 🛠️ 3. Texnologiyalar to'plami (Tech Stack)

- **Til:** Kotlin 1.9+
- **Foydalanuvchi interfeysi (UI):** Jetpack Compose, Material 3, Dark Security Theme
- **Arxitektura:** MVVM (Model-View-ViewModel) + StateFlow
- **Asinxronlik:** Kotlin Coroutines & Flow
- **Operatsion tizim talabi:** Android 7.0 (API 24) va undan yuqori (Android 14+ to'liq qo'llab-quvvatlanadi).

---

## 📂 4. Loyiha Tuzilishi

```
apk_himoyachi/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/apkhimoyachi/security/
│       │   ├── ApkHimoyachiApp.kt                # Application sinfi va xabarnomalar kanali
│       │   ├── MainActivity.kt                   # Navigatsiya va ruxsatlar boshqaruvi
│       │   ├── model/
│       │   │   ├── RiskLevel.kt                  # Xavf darajalari (SAFE, LOW, MEDIUM, HIGH, CRITICAL)
│       │   │   ├── SuspiciousReason.kt           # Xavf tushuntirish modeli
│       │   │   ├── AppSecurityInfo.kt            # O'rnatilgan ilova xavfsizlik modeli
│       │   │   └── ApkFileInfo.kt                # APK fayl xavfsizlik modeli
│       │   ├── analyzer/
│       │   │   ├── PermissionClassifier.kt       # Ruxsatlarni o'zbek tilida tasniflash va baholash
│       │   │   ├── AppRiskAnalyzer.kt            # Ilovalarni tahlil qilish va kombinatsiyalarni aniqlash
│       │   │   └── ApkFileScanner.kt             # Xotiradagi APK larni qidirish va tahlil qilish
│       │   ├── receiver/
│       │   │   └── PackageInstallReceiver.kt     # Yangi ilovalarni real vaqtda tutuvchi BroadcastReceiver
│       │   ├── service/
│       │   │   └── RealtimeProtectionService.kt  # Yuklangan fayllarni kuzatuvchi Foreground Service
│       │   └── ui/
│       │       ├── SecurityViewModel.kt          # MVVM Boshqaruvchisi
│       │       ├── theme/                        # Ranglar va mavzular (Dark Security UI)
│       │       └── screens/
│       │           ├── DashboardScreen.kt        # Bosh sahifa (Qalqon, statistika, tahlil boshlash)
│       │           ├── AppsScanScreen.kt         # Ilovalar ro'yxati va filtrlash
│       │           ├── ApkFilesScreen.kt         # APK fayllar va o'chirish oynasi
│       │           ├── AppDetailScreen.kt        # Har bir ilova bo'yicha to'liq hisobot
│       │           └── SettingsScreen.kt         # Sozlamalar va Maxfiylik deklaratsiyasi
│       └── res/
│           └── values/
│               ├── strings.xml                   # O'zbek tilidagi barcha matnlar
│               ├── colors.xml
│               └── themes.xml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 5. Ishga tushirish (Qo'llanma)

1. **Android Studio** dasturini oching.
2. `Open` tugmasini bosib quyidagi jildni tanlang:
   `C:\Users\User\.gemini\antigravity\scratch\apk_himoyachi`
3. Gradle sinxronizatsiyasi yakunlangach, emulyator yoki haqiqiy Android telefonni ulab `Run` (`Shift + F10`) tugmasini bosing.
4. Ilovada barcha funksiyalar (Tezkor tahlil, To'liq skanerlash, APK fayllarni boshqarish) to'liq tayyor holatda ishlaydi.
