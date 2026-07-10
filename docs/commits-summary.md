# MediBook — Commit History Summary (2026-07-10)

All commits on `main` branch. Working tree clean, 16 commits ahead of `origin/main`.

---

## Latest 14 Commits (This Session)

| # | Hash | Message | Files |
|---|---|---|---|---|
| 12 | `e949f80` | fix(web): move `mountedRef.current = true` to top of every effect (StrictMode remount fix) | All 5 data-fetching pages |
| 13 | `878c2d1` | fix: remove duplicate Dr. prefix — doctorName already includes title | AppointmentManagementPage.jsx, AppointmentAdapter.kt |
| 14 | `859ba8f` | feat(web): unify UI with teal/red design system across all dashboard pages | 9 files — DashboardLayout, all feature pages, index.html |
| 15 | `e9b24d1` | feat(backend): add ClinicStaff CRUD with search, update, and security rules | 6 files — DTOs, Service, Controller, Repository, SecurityConfig |
| 16 | `3dba240` | feat(web): add Staff management page with search and inline editing | ManageStaffPage.jsx, staffService.js, DashboardLayout, AppRoutes |
| --- | --- | --- | --- |
| 1 | `da52fa5` | fix(backend): add `@EnableMethodSecurity` | SecurityConfig.java |
| 2 | `64d6b14` | fix(backend): add `@Transactional(readOnly=true)` on 6 read methods | AppointmentService, DoctorScheduleService, HealthRecordService |
| 3 | `8468357` | fix(backend): scope GET /api/appointments by role, return 201 for creations | AppointmentController, DoctorScheduleController |
| 4 | `c9e14cd` | feat(backend): add bean validation to DTOs, return 201 for register | LoginRequest, RegisterRequest, HealthRecordRequest, AuthController |
| 5 | `70838a8` | perf(backend): `@EntityGraph` on appointments, DB-level overlap+search | AppointmentRepository, DoctorScheduleRepository, PatientRepository |
| 6 | `94eeb96` | feat(backend): patient mgmt, doctor update, clinic staff, record endpoints | 10 files — controllers, services, entities, repos |
| 7 | `cb85bdd` | fix(web): env URL, JSON.parse guard, ErrorBoundary, memoized auth | axiosInstance, AuthContext, ErrorBoundary, main.jsx |
| 8 | `86d1619` | feat(web): doctor queue, patient mgmt, schedule pages + stale state fix | 13 files — pages, services, routes, layout |
| 9 | `dce136e` | fix(mobile): add `@SerializedName` to all 7 model classes | All model `.kt` files across auth/appointment/doctor/schedule |
| 10 | `bbc05a5` | fix(mobile): rethrow CancellationException on all 5 activities, guard doctorId | All 5 activity `.kt` files |
| 11 | `44f0148` | feat(mobile): auto-login, logout, confirm password, HEADERS logging, 30s timeouts | TokenManager, RetrofitClient, both layouts |

---

## Full Commit History (Earliest → Latest)

### Project Setup
- `Initial commit` — repo bootstrap
- `9aa9393` — env variables, medical auth theme
- `9630897` — merge of initial setup
- `0b6c79e` — remove IntelliJ files
- `7f53d0b` — mobile login + register integration
- `d1ac0e3` — initial commit from remote

### Backend — Auth & Security
- `1df804a` — DoctorController with GET endpoints
- `0abea09` — DoctorService for listing/lookup
- `926d798` — DoctorResponse DTO + specialization search
- `454d6a3` — ResourceNotFoundException + GlobalExceptionHandler
- `c220735` — Appointment entity + status enum
- `d6b2130` — code save commit
- `ec98967` — AppointmentRepository with queries
- `e0dab57` — `findByUserUserId` in PatientRepository
- `8c108fe` — AppointmentService with transactional booking
- `ab9c742` — AppointmentController for booking/history/status
- `d9e1ba9` — role-based security rules in SecurityConfig
- `41aec90` — DoctorScheduleController
- `c34783b` — DoctorScheduleService with overlap prevention
- `0d50bcd` — DoctorScheduleRequest/Response DTOs
- `2d34c4b` — DoctorSchedule entity + pessimistic locking repo
- `fe00cb0` — AppointmentRequest/Response DTOs
- `bbb2575` — JwtException catch in JwtAuthFilter
- `5a9b575` — HealthRecordService with appointment completion
- `c98e0df` — HealthRecordController (DOCTOR write, STAFF/DOCTOR read)
- `ee6e58e` — HealthRecordService completing appointment on record creation
- `2716b44` — HealthRecord entity, one-per-appointment constraint
- `da52fa5` — **`@EnableMethodSecurity`**
- `64d6b14` — **`@Transactional(readOnly=true)` on 6 read methods**
- `8468357` — **scope appointments by role, 201 for creations**
- `c9e14cd` — **bean validation on DTOs**
- `70838a8` — **`@EntityGraph`, DB-level overlap & search**
- `94eeb96` — **patient mgmt, doctor update, clinic staff, record endpoints**

### Web — Frontend
- `c46a51f` — DashboardLayout with nav + logout
- `ff61cf0` — DoctorListPage with search + error states
- `74a2928` — doctorService for API calls
- `52ad787` — dashboard/doctors route wiring
- `200f68f` — moved pages to `/pages/doctors`
- `99a04d3` — auto-redirect on 401
- `09b1706` — moved authService to `src/services`
- `af7efd9` — AppointmentManagementPage
- `c91a6d1` — CreateSchedulePage
- `0fa13f7` — appointmentService
- `5b61acf` — scheduleService
- `6a8438c` — role-restricted schedule page
- `07251fc` — Add Schedule + Appointments nav links for staff
- `cb85bdd` — **env URL, JSON.parse guard, ErrorBoundary, memoized auth**
- `86d1619` — **doctor queue, patient mgmt, stale state fix**

### Mobile — Android
- `b3742ba` — TokenManager + AuthInterceptor
- `35a2eeb` — Doctor model
- `5d29159` — AuthInterceptor in RetrofitClient
- `ef36855` — DoctorApiService
- `e75bfe9` — activity_doctor_list + item_doctor layouts
- `58e6480` — DoctorAdapter
- `fd65770` — MedibookApplication initializer
- `74eff46` — token save + navigate after login
- `475b63c` — DoctorSchedule, AppointmentRequest/Response models
- `64f623c` — ScheduleApiService + AppointmentApiService
- `e235bd9` — DoctorScheduleListActivity with booking dialog
- `9c00b5f` — DoctorAdapter click-through navigation
- `591d5e9` — AppointmentHistoryActivity
- `a163fc4` — activity_appointment_history layout
- `e8b627f` — activity_doctor_schedule_list layout
- `e70e10e` — activity_doctor_list changes
- `d766d99` — DoctorListActivity changes
- `d6bc436` — GlobalExceptionHandler changes
- `ebf8cb4` — misc additions
- `c27d9da` — AppointmentAdapter
- `7db5257` — scheduleApi + appointmentApi in RetrofitClient
- `d868a8f` / `ac9d9db` / `982581d` / ... — vertical slice refactoring (9 commits)
- `6e06389` — AndroidManifest directory fixes
- `e1b8cd7` — search bar overlap fix
- `dce136e` — **`@SerializedName` on all models**
- `bbc05a5` — **CancellationException rethrow + doctorId guard**
- `44f0148` — **auto-login, logout, confirm password, logging level, timeouts**
