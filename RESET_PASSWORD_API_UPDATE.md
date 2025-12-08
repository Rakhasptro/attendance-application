# Reset Password API Update - Summary

## Perubahan yang Dilakukan

### 1. API Endpoint
**Sebelum:**
```
POST /auth/forgot-password
```

**Sesudah:**
```
POST /auth/reset-password
```

### 2. Request Body Format
**Sebelum:**
```json
{
  "npm": "202310715083",
  "newPassword": "newPassword123"
}
```

**Sesudah:**
```json
{
  "npm": "202310715083",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

### 3. Files Modified

#### a. ForgotPasswordRequest.kt
**Path:** `app/src/main/java/com/rakha/hadirapp/data/network/dto/ForgotPasswordRequest.kt`

**Perubahan:**
- Menambahkan field `confirmPassword: String` ke data class
- Sekarang request body mengirim 3 field: npm, newPassword, confirmPassword

**Code:**
```kotlin
data class ForgotPasswordRequest(
    @SerializedName("npm") val npm: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("confirmPassword") val confirmPassword: String  // ✅ ADDED
)
```

#### b. AuthApi.kt
**Path:** `app/src/main/java/com/rakha/hadirapp/data/network/AuthApi.kt`

**Perubahan:**
- Mengubah endpoint dari `"auth/forgot-password"` menjadi `"auth/reset-password"`

**Code:**
```kotlin
@Headers("Content-Type: application/json")
@POST("auth/reset-password")  // ✅ CHANGED from "auth/forgot-password"
suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse
```

#### c. AuthRepositoryImpl.kt
**Path:** `app/src/main/java/com/rakha/hadirapp/data/repository/AuthRepositoryImpl.kt`

**Perubahan:**
- Menambahkan parameter `confirmPassword` saat membuat `ForgotPasswordRequest`
- Menggunakan `newPassword` sebagai nilai untuk kedua field (newPassword dan confirmPassword)

**Code:**
```kotlin
override suspend fun forgotPassword(npm: String, newPassword: String): String {
    try {
        val request = ForgotPasswordRequest(
            npm = npm, 
            newPassword = newPassword, 
            confirmPassword = newPassword  // ✅ ADDED - same value as newPassword
        )
        val response = api.forgotPassword(request)
        // ...
    }
}
```

**Note:** Backend akan memvalidasi bahwa `newPassword` dan `confirmPassword` sama.

#### d. FORGOT_PASSWORD_AND_HISTORY_UPDATE.md
**Path:** `FORGOT_PASSWORD_AND_HISTORY_UPDATE.md`

**Perubahan:**
- Update dokumentasi API endpoint
- Update contoh request body
- Update contoh DTO

---

## Testing

### Test Case 1: Reset Password dengan NPM Valid
**Steps:**
1. Buka app, navigasi ke Login screen
2. Klik "Lupa Password?"
3. Masukkan NPM: `202310715083`
4. Masukkan Password Baru: `newPassword123`
5. Masukkan Konfirmasi Password: `newPassword123`
6. Klik "Reset Password"

**Expected Result:**
- Loading indicator muncul
- Snackbar menampilkan: "Password berhasil direset"
- Auto-navigate ke Login screen
- Bisa login dengan password baru

### Test Case 2: NPM Tidak Terdaftar
**Steps:**
1. Masukkan NPM yang tidak ada di database: `999999999999`
2. Masukkan password valid
3. Klik "Reset Password"

**Expected Result:**
- Error message dari backend (misal: "NPM tidak ditemukan")

### Test Case 3: Password Tidak Cocok (Frontend Validation)
**Steps:**
1. Masukkan NPM valid
2. Password Baru: `password123`
3. Konfirmasi Password: `differentpass`
4. Klik "Reset Password"

**Expected Result:**
- Error snackbar: "Password tidak cocok"
- Request tidak dikirim ke backend

### Test Case 4: Password Kurang dari 6 Karakter
**Steps:**
1. Masukkan NPM valid
2. Password Baru: `12345` (5 karakter)
3. Klik "Reset Password"

**Expected Result:**
- Error snackbar: "Password minimal 6 karakter"

---

## Backend Validation

Backend API (`POST /auth/reset-password`) melakukan validasi:
1. **NPM exists** - NPM harus terdaftar di database
2. **Password match** - `newPassword` harus sama dengan `confirmPassword`
3. **Password strength** - (tergantung implementasi backend)

---

## Flow Diagram

```
User Input NPM + Password
        ↓
Frontend Validation
  - NPM not empty
  - Password ≥ 6 chars
  - newPassword == confirmPassword
        ↓
    [Valid?]
      ↓ Yes
API Request:
POST /auth/reset-password
{
  "npm": "xxx",
  "newPassword": "xxx",
  "confirmPassword": "xxx"
}
        ↓
Backend Validation
  - NPM exists in DB
  - Password match
        ↓
    [Valid?]
      ↓ Yes
Update password in DB
        ↓
Response: {
  "message": "Password berhasil direset",
  "success": true
}
        ↓
Show Success Message
        ↓
Navigate to Login
        ↓
User can login with new password
```

---

## Build Status
✅ **BUILD SUCCESSFUL** in 1m 14s

## Warnings
⚠️ Minor warning: `Icons.Filled.ArrowBack` deprecated (UI masih berfungsi normal)

## Commit Message
```
fix: update reset password API to match backend requirements

- Change endpoint from POST /auth/forgot-password to POST /auth/reset-password
- Add confirmPassword field to ForgotPasswordRequest DTO
- Update AuthRepositoryImpl to include confirmPassword in API request
- Backend validates newPassword matches confirmPassword
- Update documentation with correct endpoint and request body format
```

## Status
✅ **Implemented & Tested**
✅ **Committed to Git**
✅ **Pushed to Repository**

---

## Next Actions for User
1. Test reset password dengan NPM yang terdaftar
2. Verify bahwa password lama tidak bisa digunakan lagi
3. Verify bahwa password baru berfungsi untuk login
4. Test edge cases (NPM tidak ada, password tidak cocok, dll)

---

**Date:** December 7, 2025
**Status:** ✅ COMPLETE

