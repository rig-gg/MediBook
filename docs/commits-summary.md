# MediBook — Commit History Summary (2026-07-15)

All commits on `main` branch.

---

## Latest 8 Commits (FR-010 Visibility + Delete Appointments — 2026-07-15)

| # | Hash | Message | Files |
|---|---|---|---|
| 1 | `2d85f50` | feat(backend): add GET /api/records/appointment/{id} endpoint with FDA suggestions | HealthRecordService, HealthRecordController, SecurityConfig |
| 2 | `10917c1` | feat(web): add getRecordByAppointment and clickable detail modal on doctor queue | recordService.js, DoctorAppointmentQueuePage.jsx |
| 3 | `2787c4d` | feat(web): add appointment detail modal with FDA suggestions on staff/admin page | AppointmentManagementPage.jsx |
| 4 | `65a955e` | feat(mobile): add HealthRecord model, RecordApiService, and RetrofitClient integration | HealthRecordResponse.kt, RecordApiService.kt, RetrofitClient.kt |
| 5 | `71591e5` | feat(mobile): add appointment detail dialog and FDA suggestion item layouts | dialog_appointment_detail.xml, item_fda_suggestion.xml |
| 6 | `9b5dbd1` | feat(mobile): add clickable appointment history with detail dialog and FDA display | AppointmentAdapter.kt, AppointmentHistoryActivity.kt |
| 7 | `f3f1e15` | feat(backend): add DELETE /api/appointments/{id} for staff/admin | AppointmentService, AppointmentController, SecurityConfig |
| 8 | `7623b77` | feat(web): add delete button for cancelled/past appointments | appointmentService.js, AppointmentManagementPage.jsx |

---

## Previous Commits (FR-010/FR-011 Session — 2026-07-13)

| # | Hash | Message | Files |
|---|---|---|---|
| 1 | `c898fb1` | Send email notification on appointment status change | EmailService, AppointmentService |
| 2 | `a834dfc` | Add async email service for appointment status notifications | EmailService |
| 3 | `aa4c0a5` | Add JavaMailSender configuration for FR-011 | EmailConfig |
| 4 | `d2e98ac` | Query OpenFDA for drug suggestions on record creation | FdaService |
| 5 | `ec10752` | Add fdaSuggestions field to health record response | HealthRecordResponse |
| 6-16 | (earlier) | Backend fixes, web/mobile hardening, audit fixes | Various |

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
