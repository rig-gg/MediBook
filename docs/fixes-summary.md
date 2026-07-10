# MediBook — Audit Fixes Summary (2026-07-10)

All 16 audit items + 2 post-fix bugs resolved. Backend (Maven) and web (Vite) build clean.

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
