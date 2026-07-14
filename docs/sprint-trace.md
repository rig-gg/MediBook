# MediBook — Full Sprint Trace

**Date:** 2026-07-15
**Branch:** `main`
**Stack:** Spring Boot 4.1.0 / Supabase PostgreSQL / React 18 + Vite + Tailwind v4 / Android Kotlin + Retrofit

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
- Staff/admin can delete cancelled or past confirmed appointments (frees schedule slot)

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

### Backend — Services

| Service | Purpose |
|---|---|
| `FdaService` | Queries OpenFDA API for drug classification suggestions based on diagnosis (FR-010) |
| `EmailService` | Sends HTML email notifications via Mailtrap SMTP on appointment status changes (FR-011) |

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
| `/api/appointments/{id}` | DELETE | STAFF/ADMIN | Delete cancelled/past appointment |
| `/api/schedules` | POST | STAFF/ADMIN | Create availability slot |
| `/api/schedules/{id}` | PUT | STAFF/ADMIN | Update slot |
| `/api/schedules` | GET | Any auth | List available slots |
| `/api/records/create` | POST | DOCTOR | Write health record (returns FDA suggestions) |
| `/api/records/appointment/{id}` | GET | DOCTOR/STAFF/PATIENT | Get record by appointment (with FDA suggestions) |
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
| `/dashboard/appointments` | AppointmentManagementPage | STAFF/ADMIN |
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
| `AppointmentHistoryActivity` | Patient's own bookings + detail dialog with FDA suggestions |

---

## 3. Current State

### ✅ Complete
- All core FRs (FR-001 through FR-011) implemented
- All business rules (BR-001 through BR-005) enforced
- Backend: all endpoints built, security configured, validation added
- Backend: ClinicStaff CRUD with search, DB-level queries, and role-based security
- Backend: OpenFDA drug classification suggestions on health record creation (FR-010)
- Backend: FDA suggestions visible on appointment detail view (doctor queue + patient history)
- Backend: SMTP email notifications via Mailtrap on appointment status changes (FR-011)
- Backend: DELETE /api/appointments/{id} for staff/admin (guards against health record FK)
- Web: all pages built, state management stable, ErrorBoundary in place
- Web: unified UI with teal/red design system across all dashboard pages
- Web: Staff management page with search and inline editing (ADMIN-only)
- Web: Clickable appointment detail modals with health record + FDA suggestions (doctor + staff)
- Web: Admin restricted from viewing clinical data (diagnosis, notes, FDA suggestions)
- Web: Delete button for cancelled/past appointments
- Mobile: all activities built, auth flow complete, network layer hardened
- Mobile: Clickable appointment history with detail dialog showing health record + FDA suggestions
- Both backend (Maven) and web (Vite) build clean

### ❌ Not Yet Implemented
(none — all FRs complete)

### ⚠️ Known Issues (Unfixed)
| Issue | Severity | Reason Skipped |
|---|---|---|
| `AuthController` contains business logic | 🟡 Medium | No service layer extracted; refactor-only, not a bug |
| PATIENT users can hit `GET /api/appointments` (URL-level) | 🟡 Low | `@PreAuthorize` now enforced, but URL rule allows it |
| Mobile `isAvailable` Boolean mapping | 🟡 Low | Already works with `@SerializedName` |
| Mobile `android:networkSecurityConfig` | 🟡 Low | Already configured for HTTP to `10.0.2.2` |

---

## 4. Key Files Reference

### Backend
| File | Purpose |
|---|---|
| `security/SecurityConfig.java` | Filter chain, role rules, `@EnableMethodSecurity` |
| `security/JwtAuthFilter.java` | JWT extraction + validation per request |
| `auth/AuthController.java` | Login + registration (has business logic — needs refactor) |
| `appointment/service/AppointmentService.java` | Booking with pessimistic lock, status transitions, delete |
| `record/service/HealthRecordService.java` | Record creation + update + get-by-appointment (FDA suggestions) |
| `schedule/service/DoctorScheduleService.java` | Schedule CRUD with DB-level overlap check |
| `patient/service/PatientService.java` | Search via DB query |
| `doctor/service/DoctorService.java` | Doctor CRUD |
| `fda/FdaService.java` | OpenFDA API integration — drug classification suggestions (FR-010) |
| `fda/FdaConfig.java` | WebClient bean with Jackson2JsonDecoder for OpenFDA |
| `email/EmailService.java` | HTML email notifications via Mailtrap SMTP (FR-011) |
| `email/EmailConfig.java` | JavaMailSender bean configuration |

### Web
| File | Purpose |
|---|---|
| `auth/AuthContext.jsx` | Auth state, memoized login/logout |
| `api/axiosInstance.js` | Axios with JWT interceptor + env URL |
| `components/ErrorBoundary.jsx` | Render error catcher |
| `components/ProtectedRoute.jsx` | Route guard by role |
| `features/appointments/recordService.js` | getRecordByAppointment for detail views |
| `features/appointments/AppointmentManagementPage.jsx` | Staff/admin: detail modal, delete, admin privacy |
| `features/appointments/DoctorAppointmentQueuePage.jsx` | Doctor: detail modal with FDA suggestions |

### Mobile
| File | Purpose |
|---|---|
| `core/network/RetrofitClient.kt` | OkHttp + Retrofit with timeouts, HEADERS logging, recordApi |
| `core/network/AuthInterceptor.kt` | Attaches JWT to requests |
| `core/utils/TokenManager.kt` | SharedPreferences session (token, userId, name, role) |
| `feature/appointment/model/HealthRecordResponse.kt` | HealthRecord + FdaDrugSuggestion models |
| `feature/appointment/network/RecordApiService.kt` | Retrofit interface for GET /api/records/appointment/{id} |
| `feature/appointment/ui/AppointmentHistoryActivity.kt` | Detail dialog with health record + FDA suggestions |

---

## 5. Next Steps

1. ~~**FR-010** — `OpenFDA service`~~ — ✅ Complete (2026-07-13)
2. ~~**FR-010 visibility** — Surface FDA suggestions in web + mobile~~ — ✅ Complete (2026-07-15)
3. ~~**FR-011** — `Email service`~~ — ✅ Complete (2026-07-13)
4. **Optional cleanup** — Extract `AuthService` from `AuthController`; add `@PreAuthorize` URL rules for defense in depth
5. **Optional** — Schedule editing by staff (currently admin-only provisioning)
