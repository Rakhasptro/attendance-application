# Update: Attendance History & Forgot Password Feature

## Overview
Implemented two major features:
1. **Display course name in attendance history** instead of student name
2. **Forgot password feature** for password reset using NPM

---

## 1. Attendance History - Course Name Display

### Changes Made:

#### DTO Updates:
- **AttendanceHistoryResponse.kt**
  - Added `Schedule` data class with fields:
    - `courseName` - Nama mata kuliah
    - `courseCode` - Kode mata kuliah
    - `date`, `startTime`, `endTime` - Jadwal
    - `room` - Ruangan
    - `status` - Status jadwal
  - Updated `AttendanceHistoryItem` to include `schedule: Schedule?` field

#### UI Updates:
- **HomeScreen.kt**
  - Changed history card to display `item.schedule?.courseName` instead of `item.studentName`
  - Shows "Unknown Course" if course name is null
  
#### Search Updates:
- **HomeViewModel.kt**
  - Updated `filterHistory()` to search by:
    - Course name (`schedule.courseName`)
    - Student name
    - Student NPM

### API Response Format:
```json
{
  "id": "...",
  "studentName": "Rakha adi saputro",
  "studentNpm": "202310715083",
  "schedule": {
    "courseName": "Keamanan Siber",
    "courseCode": "F5A7",
    "date": "2025-12-06T00:00:00.000Z",
    "startTime": "21:00",
    "endTime": "22:00",
    "room": "SS-405",
    "status": "ACTIVE"
  }
}
```

---

## 2. Forgot Password Feature

### API Endpoint:
```
POST /auth/reset-password
Content-Type: application/json

Body:
{
  "npm": "202310715083",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}

Response:
{
  "message": "Password berhasil direset",
  "success": true
}
```

### Files Created:

#### 1. **ForgotPasswordRequest.kt** (DTO)
```kotlin
data class ForgotPasswordRequest(
    @SerializedName("npm") val npm: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)

data class ForgotPasswordResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean? = null
)
```

#### 2. **ForgotPasswordViewModel.kt**
- State management dengan `ForgotPasswordUiState`:
  - `Idle` - Initial state
  - `Loading` - Saat proses reset
  - `Success` - Berhasil reset password
  - `Error` - Error dengan pesan

- Validation logic:
  - NPM tidak boleh kosong
  - Password minimal 6 karakter
  - Password dan konfirmasi password harus sama

- Auto-navigate ke login setelah sukses

#### 3. **ForgotPasswordScreen.kt**
- Modern UI dengan animasi fade-in dan slide-in
- Form fields:
  - **NPM** - Input NPM yang terdaftar
  - **Password Baru** - Input password baru
  - **Konfirmasi Password** - Re-enter password
- **Reset Password** button dengan loading state
- **Kembali ke Login** link
- Back button di top bar
- Snackbar untuk error dan success messages

### Integration:

#### AuthApi.kt
```kotlin
@POST("auth/reset-password")
suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse
```

#### AuthRepository.kt & AuthRepositoryImpl.kt
```kotlin
suspend fun forgotPassword(npm: String, newPassword: String): String
```

#### LoginScreen.kt
- Added "Lupa Password?" link after password field
- Navigates to `forgot_password` route

#### NavGraph.kt
- Added `ForgotPasswordViewModel` initialization
- Added `forgot_password` composable route

---

## UI/UX Features:

### Forgot Password Screen:
- **Design**: Consistent with Login/Register screens
- **Logo**: App logo at top
- **Title**: "Reset Password"
- **Subtitle**: "Masukkan NPM dan password baru Anda"
- **Color Scheme**: Primary Blue (#0C5AFF)
- **Animations**: 
  - Fade-in effect (900ms)
  - Slide-in animations for each element with delays
- **Validation Feedback**: Real-time via snackbar
- **Success Flow**: Shows success message → Auto-navigate to login

### Validation Rules:
1. NPM must not be empty
2. NPM must exist in database (validated by backend)
3. New password must be at least 6 characters
4. Confirm password must match new password

---

## Testing Instructions:

### Test Forgot Password:
1. Launch app and navigate to Login screen
2. Click "Lupa Password?" link
3. Enter registered NPM (e.g., "202310715083")
4. Enter new password (minimum 6 characters)
5. Re-enter same password in confirm field
6. Click "Reset Password"
7. Verify success message appears
8. Verify auto-navigation to login screen
9. Try logging in with new password

### Test Validation:
1. Try empty NPM → Should show error
2. Try password < 6 chars → Should show error
3. Try mismatched passwords → Should show error
4. Try non-existent NPM → Should show backend error

### Test Attendance History:
1. Login to app
2. Navigate to Home screen
3. Verify history cards show course names (e.g., "Keamanan Siber")
4. Test search with:
   - Course name
   - Student name
   - NPM
5. Verify filtered results

---

## Git Commit:
```
feat: display course name in attendance history and add forgot password feature

- Update AttendanceHistoryItem DTO to include Schedule object with course details
- Modify HomeScreen to display courseName instead of studentName in history cards
- Update HomeViewModel search to filter by course name, student name, or NPM
- Add forgot password feature:
  * Create ForgotPasswordRequest and ForgotPasswordResponse DTOs
  * Add forgotPassword endpoint to AuthApi (POST /auth/forgot-password)
  * Implement forgotPassword in AuthRepository and AuthRepositoryImpl
  * Create ForgotPasswordViewModel with validation logic
  * Create ForgotPasswordScreen UI with NPM, new password, and confirm password fields
  * Add forgot password link in LoginScreen
  * Add forgot_password route to NavGraph
- Validation includes: NPM not empty, password minimum 6 characters, passwords match
- Auto-navigate to login screen after successful password reset
```

---

## Files Modified:
1. `AuthApi.kt` - Added forgotPassword endpoint
2. `AuthRepository.kt` - Added interface method
3. `AuthRepositoryImpl.kt` - Added implementation
4. `AttendanceHistoryResponse.kt` - Added Schedule data class
5. `HomeScreen.kt` - Display course name instead of student name
6. `HomeViewModel.kt` - Updated search filter
7. `LoginScreen.kt` - Added forgot password link
8. `NavGraph.kt` - Added ForgotPasswordViewModel and route

## Files Created:
1. `ForgotPasswordRequest.kt` - Request/Response DTOs
2. `ForgotPasswordViewModel.kt` - ViewModel with validation
3. `ForgotPasswordScreen.kt` - UI screen

---

## Build Status:
✅ **BUILD SUCCESSFUL**

## Implementation Date:
December 7, 2025

## Status:
✅ Committed & Pushed to Repository

---

## Next Steps (Optional Enhancements):
1. Add OTP verification via email/SMS
2. Add password strength indicator
3. Add rate limiting for reset attempts
4. Add email notification after password change
5. Add password history (prevent reusing old passwords)
6. Add lecturer name display in attendance history (fetch from confirmedBy)

