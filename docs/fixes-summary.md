# MediBook — Audit Fixes Summary (2026-07-15)

All 16 original audit items + 2 post-fix bugs + 3 FR-010/FR-011 fixes + 2 new features resolved. Backend (Maven) and web (Vite) build clean.

---

## Backend (10 fixes)

### Critical
| Issue | Fix | File |
|---|---|---|
| `@PreAuthorize` silently ignored | Added `@EnableMethodSecurity` to `SecurityConfig` | `backend/.../security/SecurityConfig.java` |
| Lazy-loading outside transactions | Added `@Transactional(readOnly = true)` to 6 read methods | `AppointmentService`, `DoctorScheduleService`, `HealthRecordService` |
| `GET /api/appointments` returns ALL to anyone | Controller now returns own appointments for PATIENT role | `AppointmentController.java:40-47` |
| No validation on DTOs | Added `@NotBlank`, `@Email`, `@Size`, `@NotNull` annotations | `RegisterRequest`, `LoginRequest`, `HealthRecordRequest` |
| `@Valid` not on controller params | Added `@Valid` to all 3 auth endpoints | `AuthController.java` |

### High/Medium
| Issue | Fix | File |
|---|---|---|
| Creation endpoints return 200 | Changed to 201: `bookAppointment`, `register`, `admin-register`, `createSchedule` | Controllers |
| N+1 queries on appointments | `@EntityGraph(attributePaths = {"patient", "schedule.doctor"})` | `AppointmentRepository.java` |
| Overlap check loads all rows | DB-level query `existsOverlapping()` instead of in-memory stream | `DoctorScheduleRepository.java` |
| Patient search loads all patients | DB query `searchPatients()` + `findAllWithUser()` with `@EntityGraph` | `PatientRepository.java` |
| Redundant `save()` calls | Removed `scheduleRepository.save()` — JPA dirty check handles it | `AppointmentService.java` |
| `PUT /api/records/**` unrestricted | Added `hasRole("DOCTOR")` to SecurityConfig | `SecurityConfig.java:65` |

---

## Web (5 fixes)

| Issue | Fix | File |
|---|---|---|
| Hardcoded API URL | Moved to `VITE_API_URL` env variable + `.env` file | `axiosInstance.js:4`, `.env` |
| `JSON.parse` crash on bad localStorage | try/catch with session cleanup | `AuthContext.jsx:15-18` |
| Race conditions in `useEffect` | `useRef` mounted guard on all 5 data-fetching pages | `DoctorListPage`, `ManageDoctorsPage`, `ManagePatientsPage`, `AppointmentManagementPage`, `DoctorAppointmentQueuePage` |
| No Error Boundary | Created `<ErrorBoundary>` component, wired in `main.jsx` | `components/ErrorBoundary.jsx` |
| `login`/`logout` not memoized, `useAuth` unsafe | `useCallback` wrappers, null guard throws outside provider | `AuthContext.jsx` |

---

## Mobile (7 fixes)

| Issue | Fix | File(s) |
|---|---|---|
| No `@SerializedName` | Added to all 7 model classes | `AuthResponse`, `LoginRequest`, `RegisterRequest`, `AppointmentRequest`, `AppointmentResponse`, `Doctor`, `DoctorSchedule` |
| `CancellationException` swallowed | `catch (e: CancellationException) -> throw e` on all 5 activities | All activities |
| No auto-login | `LoginActivity.onCreate` checks `TokenManager.isLoggedIn()` | `LoginActivity.kt:29-33` |
| No logout | Added logout button with confirmation dialog | `DoctorListActivity.kt`, `activity_doctor_list.xml` |
| HTTP body logging on release | Changed to `HEADERS` level, conditional on `Log.isLoggable` | `RetrofitClient.kt:19-21` |
| No network timeouts | Added 30s connect/read/write timeouts | `RetrofitClient.kt:24-27` |
| No confirm password | Added `etConfirmPassword` field + `password != confirmPassword` check | `RegisterActivity.kt`, `activity_register.xml` |
| `userId` not persisted | Added `KEY_USER_ID` to `TokenManager`, updated `saveSession` | `TokenManager.kt` |
| `doctorId = -1L` sent to API | Added early return guard | `DoctorScheduleListActivity.kt:32-35` |

---

## Post-Audit Fixes (2)

| Issue | Fix | File |
|---|---|---|
| **StrictMode remount — pages stuck on "Loading..."** | `mountedRef.current = true` moved to top of all 5 effects (was after the fetch call, so on remount it stayed `true`) | All 5 data-fetching pages |
| **Duplicate "Dr." prefix** | `doctorName` stored as `Dr. Jeff`; UI rendered `Dr. Dr. Jeff`. Removed manual `Dr.` prefix from both web and mobile | `web/.../AppointmentManagementPage.jsx:92`, `mobile/.../AppointmentAdapter.kt` |

---

## FR-010/FR-011 Fixes (3)

| Issue | Fix | File |
|---|---|---|
| **WebClient JsonNode deserialization failure** | Added `Jackson2JsonDecoder(new ObjectMapper())` to WebClient builder — `JsonNode` deserializer deprecated in SB4.1 but required for OpenFDA response parsing | `fda/FdaConfig.java` |
| **FDA query returns empty results** | Changed search from `indication:` to `description:` (more reliable for matching diagnoses); added `spl_product_data_elements` fallback when `openfda.brand_name`/`generic_name` are null | `fda/FdaService.java` |
| **EmailConfig SSL handshake failure** | Removed `mail.smtp.ssl.enable=true` (port 465 config); kept `mail.smtp.starttls.enable=true` for port 587 | `email/EmailConfig.java` |

---

## New Features (2026-07-15)

### FR-010 Visibility — FDA Suggestions on Appointment Detail View

| Change | File(s) |
|---|---|
| Backend: `GET /api/records/appointment/{id}` endpoint returns health record + re-queried FDA suggestions | `HealthRecordService.java`, `HealthRecordController.java` |
| Backend: PATIENT role added to records/appointment GET security rule | `SecurityConfig.java` |
| Web: `getRecordByAppointment()` in recordService | `recordService.js` |
| Web: Doctor queue — clickable appointment rows → detail modal with health record + FDA suggestions | `DoctorAppointmentQueuePage.jsx` |
| Web: Staff/admin — clickable appointment rows → detail modal with health record + FDA suggestions | `AppointmentManagementPage.jsx` |
| Web: Admin restricted from viewing clinical data (diagnosis, notes, FDA suggestions) | `AppointmentManagementPage.jsx` |
| Mobile: `HealthRecordResponse` + `FdaDrugSuggestion` models | `HealthRecordResponse.kt` |
| Mobile: `RecordApiService` Retrofit interface | `RecordApiService.kt` |
| Mobile: `recordApi` added to RetrofitClient | `RetrofitClient.kt` |
| Mobile: Detail dialog layout + FDA suggestion item layout | `dialog_appointment_detail.xml`, `item_fda_suggestion.xml` |
| Mobile: Clickable appointment history → detail dialog with health record + FDA suggestions | `AppointmentAdapter.kt`, `AppointmentHistoryActivity.kt` |

### Delete Stale Appointments

| Change | File(s) |
|---|---|
| Backend: `AppointmentService.deleteAppointment()` — guards against health record FK, frees schedule slot | `AppointmentService.java` |
| Backend: `DELETE /api/appointments/{id}` endpoint (staff/admin only) | `AppointmentController.java` |
| Backend: DELETE security rule added | `SecurityConfig.java` |
| Web: `deleteAppointment()` in appointmentService | `appointmentService.js` |
| Web: Delete button for cancelled/past confirmed appointments (list + detail modal) | `AppointmentManagementPage.jsx` |

---

## JWT Security Hardening (2026-07-15)

| Change | File(s) |
|---|---|
| Rotated JWT secret to 512-bit random key; fail-fast on startup if < 43 chars | `application.properties`, `.env`, `.env.example`, `JwtProperties.java` |
| Access token lifetime: 24h → 30min; refresh token: 7 days with rotation | `JwtUtil.java`, `application.properties` |
| Server-side token blacklist (JTI-based) for logout/revocation | `TokenBlacklistService.java` |
| Rate limiting: 5 attempts/min per IP on login + patient registration | `RateLimitService.java`, `AuthController.java` |
| POST /api/auth/refresh — validates refresh token, returns new access + refresh token | `AuthController.java`, `RefreshTokenRequest.java` |
| POST /api/auth/logout — blacklists access + refresh token JTIs | `AuthController.java`, `LogoutRequest.java` |
| JwtAuthFilter checks blacklist + rejects non-access tokens | `JwtAuthFilter.java` |
| AuthResponse: `token` → `accessToken` + `refreshToken` | `AuthResponse.java` |
| Web: axios interceptor auto-refreshes on 401 before redirect | `axiosInstance.js`, `AuthContext.jsx`, `authService.js` |
| Mobile: AuthInterceptor attempts token refresh on 401 | `AuthInterceptor.kt`, `AuthApiService.kt`, `AuthResponse.kt` |
| Mobile: TokenManager migrated to EncryptedSharedPreferences | `TokenManager.kt`, `build.gradle.kts` |
| Mobile: LoginActivity + RegisterActivity updated for new auth response | `LoginActivity.kt`, `RegisterActivity.kt` |
