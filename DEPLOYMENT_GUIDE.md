# 📘 Panduan Deployment - Mengganti Base URL

## 🎯 Masalah yang Diselesaikan

Sebelumnya, jika Anda ingin mengganti URL backend (misalnya dari emulator ke hosting production), Anda harus mengubah URL di banyak tempat:
- ❌ HomeScreen.kt → hardcoded baseUrl
- ❌ NetworkModule.kt → hardcoded BASE_URL
- ❌ Dan mungkin file lainnya...

**Sekarang sudah terpusat!** ✅

---

## 🔧 Cara Mengganti Base URL (Single Source of Truth)

### Langkah 1: Edit `gradle.properties`

Buka file: `gradle.properties` (di root project)

```properties
# Base URL untuk backend API
BASE_URL=http://10.0.2.2:3000/api/
```

### Langkah 2: Ganti dengan URL Hosting Anda

#### Contoh untuk Development (Emulator):
```properties
BASE_URL=http://10.0.2.2:3000/api/
```

#### Contoh untuk Testing (Real Device - WiFi lokal):
```properties
BASE_URL=http://192.168.0.4:3000/api/
```

#### Contoh untuk Production (Hosting):
```properties
BASE_URL=https://api.yourapp.com/api/
```

### Langkah 3: Clean & Rebuild

```bash
./gradlew clean assembleDebug
```

**Selesai!** Seluruh aplikasi sekarang menggunakan URL baru.

---

## 📁 File yang Sudah Menggunakan BuildConfig.BASE_URL

✅ **NetworkModule.kt** - Semua API endpoint (AuthApi, ProfileApi, AttendanceApi)
✅ **HomeScreen.kt** - Image loading untuk attendance history
✅ **Semua Retrofit calls** - Otomatis menggunakan base URL yang sama

---

## 🚀 Deployment Scenarios

### Scenario 1: Development dengan Android Emulator
```properties
BASE_URL=http://10.0.2.2:3000/api/
```
- `10.0.2.2` = localhost dari perspective emulator
- Backend berjalan di laptop Anda

### Scenario 2: Testing dengan Real Device (Local Network)
```properties
BASE_URL=http://192.168.0.4:3000/api/
```
- Ganti `192.168.0.4` dengan IP laptop Anda di WiFi
- Device dan laptop harus di WiFi yang sama
- Cara cek IP: `ipconfig` (Windows) atau `ifconfig` (Mac/Linux)

### Scenario 3: Production dengan Hosting
```properties
BASE_URL=https://api.yourapp.com/api/
```
- Gunakan domain hosting Anda
- Pastikan menggunakan HTTPS untuk keamanan
- Contoh hosting: Railway, Heroku, AWS, DigitalOcean, dll

### Scenario 4: Staging Server
```properties
BASE_URL=https://staging-api.yourapp.com/api/
```
- Server testing sebelum production

---

## 🔒 Build Variants (Advanced)

Jika ingin berbeda URL untuk debug dan release, edit `app/build.gradle.kts`:

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/api/\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://api.yourapp.com/api/\"")
        }
    }
}
```

---

## 📝 Checklist Sebelum Deploy

- [ ] Ganti `BASE_URL` di `gradle.properties`
- [ ] Pastikan backend sudah running di URL yang baru
- [ ] Test login endpoint terlebih dahulu
- [ ] Clean build: `./gradlew clean`
- [ ] Build APK: `./gradlew assembleDebug` atau `assembleRelease`
- [ ] Install dan test di device
- [ ] Verify semua fitur berfungsi:
  - [ ] Login
  - [ ] Register
  - [ ] Profile load
  - [ ] Scan QR & Attendance
  - [ ] History dengan foto selfie

---

## 🐛 Troubleshooting

### Error: "Unable to resolve host"
**Solusi:**
- Pastikan backend sudah running
- Cek URL di browser/Postman dulu
- Untuk real device, pastikan di WiFi yang sama

### Error: "Cleartext HTTP not permitted"
**Solusi:**
- Gunakan HTTPS di production, atau
- Tambahkan `android:usesCleartextTraffic="true"` di AndroidManifest.xml (hanya untuk development)

### Foto selfie tidak muncul di history
**Solusi:**
- Pastikan BASE_URL di `gradle.properties` sudah benar
- BuildConfig akan otomatis remove `/api/` suffix untuk image URLs
- Cek di Logcat apakah URL image sudah benar

---

## ✅ Keuntungan Centralized Configuration

✨ **Single Source of Truth**
- Ubah 1 tempat saja → seluruh app update

✨ **Easy Deployment**
- Development → Staging → Production tinggal ganti 1 baris

✨ **No More Hardcoded URLs**
- Semua URL dari BuildConfig.BASE_URL

✨ **Version Control Friendly**
- Bisa commit tanpa khawatir expose URL production

---

## 📞 Support

Jika ada masalah dengan deployment, cek:
1. Log di Android Studio (Logcat)
2. Network traffic dengan OkHttp interceptor
3. Backend logs

Happy Deploying! 🚀

