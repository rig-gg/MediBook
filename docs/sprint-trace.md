# MediBook — Full Sprint Trace

**Date:** 2026-07-10
**Branch:** `main` (16 ahead of `origin/main`)
**Stack:** Spring Boot 3.x / Supabase PostgreSQL / React 18 + Vite + Tailwind v4 / Android Kotlin + Retrofit

---

## 1. Architecture Overview

```
MediBook/
├── backend/       Spring Boot 3.x, JWT auth, BCrypt, Lombok, Supabase PostgreSQL
├── web/           React 18 + Vite + Tailwind CSS v4, runs on :5173
├── mobile/        Android Kotlin, Retrofit 2.11 + Gson, Material Design, min SDK 26
└── docs/
```

### Auth Flow
- JWT issued on login/register, passed as `Authorization: Bearer <token>`
- Four roles: `PATIENT`, `STAFF`, `DOCTOR`, `ADMIN`
- `ADMIN` provisions `STAFF`/`DOCTOR` accounts; `PATIENT` self-registers

### Appointment State Machine
```
PENDING → CONFIRMED → COMPLETED
  ↓           ↓
CANCELLED   CANCELLED
```
- One immutable `HealthRecord` per completed appointment (BR-004)
- Pessimistic write lock prevents double-booking (BR-003 / BR-006)

---

## 2. What Was Built (Features)

### Backend — Entities & Repositories

| Entity | Table | Key Relations |
|---|---|---|
| `User` | `users` | Base auth entity, role discriminator |
| `Patient` | `patients` | `@OneToOne` → User |
| `Doctor` | `doctors` | `@OneToOne` → User |
| `ClinicStaff` | `clinic_staff` | `@OneToOne` → User |
| `DoctorSchedule` | `doctor_schedules` | `@ManyToOne` → Doctor, `PESSIMISTIC_WRITE` lock |
| `Appointment` | `appointments` | `@ManyToOne` → Patient/Schedule, unique schedule constraint |
| `HealthRecord` | `health_records` | `@OneToOne` → Appointment, `@ManyToOne` → Doctor/Patient |

### Backend — Controllers & Endpoints

| Endpoint | Method | Roles | Purpose |
|---|---|---|---|
| `/api/auth/login` | POST | All | Login |
| `/api/auth/register/patient` | POST | All | Patient self-registration |
| `/api/admin/register` | POST | ADMIN | Provision STAFF/DOCTOR |
| `/api/appointments` | POST | PATIENT | Book appointment |
| `/api/appointments` | GET | STAFF/DOCTOR/ADMIN | List all (filtered by status) |
| `/api/appointments/me` | GET | PATIENT | Own appointment history |
| `/api/appointments/doctor/{id}` | GET | Any auth | Doctor queue (frontend-guarded) |
| `/api/appointments/{id}/status` | PATCH | STAFF/ADMIN | Approve/cancel/complete |
| `/api/schedules` | POST | STAFF/ADMIN | Create availability slot |
| `/api/schedules/{id}` | PUT | STAFF/ADMIN | Update slot |
| `/api/schedules` | GET | Any auth | List available slots |
| `/api/records/create` | POST | DOCTOR | Write health record |
| `/api/records/patient/{id}` | GET | DOCTOR/STAFF | Read patient records |
| `/api/records/{id}` | PUT | DOCTOR | Update diagnosis/notes |
| `/api/doctors` | GET | Any auth | List doctors |
| `/api/doctors/{id}` | GET | Any auth | Doctor detail |
| `/api/doctors/user/{id}` | GET | Any auth | Doctor by user ID |
| `/api/doctors/{id}` | PUT | ADMIN/STAFF | Update doctor |
| `/api/staff` | GET | ADMIN/STAFF/DOCTOR | List/search staff |
| `/api/staff/{id}` | GET | ADMIN/STAFF/DOCTOR | Staff detail |
| `/api/staff/user/{id}` | GET | ADMIN/STAFF/DOCTOR | Staff by user ID |
| `/api/staff/{id}` | PUT | ADMIN | Update staff |
| `/api/patients` | GET | ADMIN/STAFF/DOCTOR | List/search patients |
| `/api/patients/{id}` | GET | ADMIN/STAFF/DOCTOR | Patient detail |

### Web — Pages & Routes

| Route | Page | Roles |
|---|---|---|
| `/login` | LoginPage | All |
| `/admin/register` | AdminRegisterPage | ADMIN |
| `/doctors` | DoctorListPage | All auth |
| `/dashboard/appointments` | AppointmentManagementPage | STAFF |
| `/dashboard/schedules` | CreateSchedulePage | STAFF/ADMIN |
| `/dashboard/patients` | ManagePatientsPage | ADMIN/STAFF/DOCTOR |
| `/dashboard/doctors` | ManageDoctorsPage | ADMIN/STAFF |
| `/staff` | ManageStaffPage | ADMIN |
| `/doctor/queue` | DoctorAppointmentQueuePage | DOCTOR |

### Mobile — Activities

| Activity | Purpose |
|---|---|
| `LoginActivity` | Login with auto-login |
| `RegisterActivity` | Patient registration with confirm password |
| `DoctorListActivity` | Doctor directory with specialization filter |
| `DoctorScheduleListActivity` | Available slots + booking dialog |
| `AppointmentHistoryActivity` | Patient's own bookings |

---

## 3. Today's Fixes (Audit-Driven)

### 🔴 Critical
| Issue | Fix |
|---|---|
| `@PreAuthorize` silently ignored | Added `@EnableMethodSecurity` to SecurityConfig |
| No `@Transactional` on 6 read methods | Added `@Transactional(readOnly = true)` |
| `GET /api/appointments` leaked all data | Controller returns own appointments for PATIENT |
| Mobile models had no `@SerializedName` | Added to all 7 model classes |
| `CancellationException` swallowed on all activities | Re-thrown on all 5 activities |

### 🟠 High
| Issue | Fix |
|---|---|
| Hardcoded API URL (`localhost:8080`) | Moved to `VITE_API_URL` env var |
| `JSON.parse` crashes on bad localStorage | try/catch + session cleanup |
| Race conditions in all `useEffect` hooks | `useRef` mounted guard on all 5 pages |
| Missing Error Boundary | Created `<ErrorBoundary>`, wired in `main.jsx` |
| No auto-login on mobile | `LoginActivity` skips to main screen if token exists |
| No logout on mobile | Button with confirmation dialog added |
| HTTP body logging leaks credentials | Changed to `HEADERS`, conditional on `Log.isLoggable` |

### 🟡 Medium
| Issue | Fix |
|---|---|
| N+1 queries on appointments | `@EntityGraph` eager-fetches patient + schedule.doctor |
| Schedule overlap check loads all rows | Replaced with DB-level `COUNT` query |
| Patient search loads all patients | Replaced with DB `LIKE` query |
| `save()` called on managed entities | Removed redundant `scheduleRepository.save()` calls |
| Creation endpoints return 200 | Changed to 201 for all 4 creation endpoints |
| No `@Valid` on controller params | Added to all public endpoints |
| DTOs missing validation annotations | Added `@NotBlank`, `@Email`, `@Size` |
| No network timeouts on mobile | 30s connect/read/write timeouts |
| No confirm password on register | Added field + validation |
| `doctorId = -1L` sent unchecked | Early return guard |
| `userId` not persisted in TokenManager | Added `KEY_USER_ID` + 4-arg `saveSession` |
| `login`/`logout` not memoized | Wrapped in `useCallback` |
| `useAuth` silently returns `null` | Now throws if used outside `AuthProvider` |

---

## 4. Current State

### ✅ Complete
- All core FRs (FR-001 through FR-009) implemented
- All business rules (BR-001 through BR-005) enforced
- Backend: all endpoints built, security configured, validation added
- Backend: ClinicStaff CRUD with search, DB-level queries, and role-based security
- Web: all pages built, state management stable, ErrorBoundary in place
- Web: unified UI with teal/red design system across all dashboard pages
- Web: Staff management page with search and inline editing (ADMIN-only)
- Mobile: all activities built, auth flow complete, network layer hardened
- Both backend (Maven) and web (Vite) build clean

### ❌ Not Yet Implemented
| FR | Description |
|---|---|
| **FR-010** | OpenFDA API integration — server-to-server call for medical classifications when doctor types diagnosis |
| **FR-011** | SMTP email notifications via Mailtrap on appointment status changes |

### ⚠️ Known Issues (Unfixed)
| Issue | Severity | Reason Skipped |
|---|---|---|
| `AuthController` contains business logic | 🟡 Medium | No service layer extracted; refactor-only, not a bug |
| PATIENT users can hit `GET /api/appointments` (URL-level) | 🟡 Low | `@PreAuthorize` now enforced, but URL rule allows it |
| Mobile `isAvailable` Boolean mapping | 🟡 Low | Already works with `@SerializedName` |
| Mobile `android:networkSecurityConfig` | 🟡 Low | Already configured for HTTP to `10.0.2.2` |

---

## 5. Key Files Reference

### Backend
| File | Purpose |
|---|---|
| `security/SecurityConfig.java` | Filter chain, role rules, `@EnableMethodSecurity` |
| `security/JwtAuthFilter.java` | JWT extraction + validation per request |
| `auth/AuthController.java` | Login + registration (has business logic — needs refactor) |
| `appointment/service/AppointmentService.java` | Booking with pessimistic lock, status transitions |
| `record/service/HealthRecordService.java` | Record creation + update (BR-004 enforced) |
| `schedule/service/DoctorScheduleService.java` | Schedule CRUD with DB-level overlap check |
| `patient/service/PatientService.java` | Search via DB query |
| `doctor/service/DoctorService.java` | Doctor CRUD |

### Web
| File | Purpose |
|---|---|
| `auth/AuthContext.jsx` | Auth state, memoized login/logout |
| `api/axiosInstance.js` | Axios with JWT interceptor + env URL |
| `components/ErrorBoundary.jsx` | Render error catcher |
| `components/ProtectedRoute.jsx` | Route guard by role |

### Mobile
| File | Purpose |
|---|---|
| `core/network/RetrofitClient.kt` | OkHttp + Retrofit with timeouts, HEADERS logging |
| `core/network/AuthInterceptor.kt` | Attaches JWT to requests |
| `core/utils/TokenManager.kt` | SharedPreferences session (token, userId, name, role) |

---

## 5b. Post-Audit Fixes (completed)

| Bug | Root Cause | Fix |
|---|---|---|
| Pages stuck on "Loading..." in StrictMode | `mountedRef.current = true` ran after fetch; on remount it stayed `true` | Moved to top of all 5 effects so it resets on every mount |
| "Dr. Dr. Jeff" — duplicate title | `doctorName` stored as `Dr. Jeff`, UI prepended `Dr. ` | Removed manual prefix from web & mobile |

---

## 6. Next Steps

1. **FR-010** — `OpenFDA service`: server-to-server HTTP call (`WebClient`/`RestTemplate`) from `HealthRecordService` when doctor saves diagnosis; return drug/condition classifications as read-only suggestions
2. **FR-011** — `Email service`: Spring Mail + Mailtrap SMTP; send on appointment status change (`PENDING → CONFIRMED`, `CONFIRMED → CANCELLED`, etc.)
3. **Optional cleanup** — Extract `AuthService` from `AuthController`; add `@PreAuthorize` URL rules for defense in depth
