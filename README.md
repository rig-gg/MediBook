# MediBook: Health Appointment & Records Integration System

**Course:** IT342-[G01] Systems Integration and Architecture 1
**Developer:** Gyle M. Amihan

## Overview

MediBook is an integrated client-server healthcare ecosystem built to replace manual appointment tracking and disconnected record-keeping in small clinics and barangay health centers. It connects patients, clinic staff, doctors, and administrators through a single platform backed by a centralized REST API and Supabase PostgreSQL database.

The system is made up of three parts:

- **Backend API** — Spring Boot REST API handling auth, appointments, schedules, records, and staff/doctor/patient management
- **Web Application** — React + Vite dashboard for clinic staff, doctors, and administrators (teal/red design system)
- **Mobile Application** — Android Kotlin app for patient self-service (registration, browsing doctors, booking appointments)

## Repository Structure

```
medibook/
 ├── backend/     # Spring Boot REST API (Maven, Java 17)
 ├── web/         # React + Vite dashboard (Tailwind CSS v4)
 ├── mobile/      # Android Kotlin app (Retrofit, min SDK 26)
 └── docs/        # SRS, diagrams, sprint traces, commit history
```

## Tech Stack

| Layer | Technology |
|---|---|
| Backend API | Spring Boot 4.1.0, Spring Security, Spring Data JPA |
| Database | Supabase PostgreSQL |
| Authentication | JWT (jjwt 0.12.5) + BCrypt password hashing |
| Web App | React 18, Vite 5, Tailwind CSS v4, React Router 7, Axios |
| Mobile App | Android (Kotlin), Retrofit 2.11, OkHttp 4.12, Coroutines |
| Documentation | UML diagrams (class, activity, sequence, use case, ERD) |

## User Roles

| Role | Access | Capabilities |
|---|---|---|
| **PATIENT** | Mobile app | Self-register, login, browse doctors, view available slots, book/cancel appointments, view history |
| **STAFF** | Web dashboard | Manage appointments (approve/cancel/complete), create doctor schedules, view patients and doctors |
| **DOCTOR** | Web dashboard | View personal appointment queue, write health records with diagnosis and notes, browse patients |
| **ADMIN** | Web dashboard | Provision STAFF/DOCTOR accounts, manage staff profiles, full STAFF capabilities |

## API Endpoints

### Auth
| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Authenticate any user, returns JWT |
| POST | `/api/auth/register/patient` | Public | Patient self-registration (mobile) |
| POST | `/api/admin/register` | ADMIN | Provision STAFF or DOCTOR accounts |

### Appointments
| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/appointments` | PATIENT | Book appointment |
| GET | `/api/appointments/me` | PATIENT | View own appointments |
| GET | `/api/appointments` | STAFF/DOCTOR/ADMIN | List all (filter by status) |
| GET | `/api/appointments/doctor/{id}` | Authenticated | Doctor queue |
| PATCH | `/api/appointments/{id}/status` | STAFF/ADMIN | Approve/cancel/complete |

### Schedules
| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/schedules` | STAFF/ADMIN | Create availability slot |
| PUT | `/api/schedules/{id}` | STAFF/ADMIN | Update slot |
| GET | `/api/schedules` | Authenticated | List available slots |

### Doctors
| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/doctors` | Authenticated | List doctors (filter by specialization) |
| GET | `/api/doctors/{id}` | Authenticated | Doctor detail |
| GET | `/api/doctors/user/{id}` | Authenticated | Doctor by user ID |
| PUT | `/api/doctors/{id}` | ADMIN/STAFF | Update doctor |

### Patients
| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/patients` | ADMIN/STAFF/DOCTOR | List/search patients |
| GET | `/api/patients/{id}` | ADMIN/STAFF/DOCTOR | Patient detail |

### Staff
| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/staff` | ADMIN/STAFF/DOCTOR | List/search staff |
| GET | `/api/staff/{id}` | ADMIN/STAFF/DOCTOR | Staff detail |
| GET | `/api/staff/user/{id}` | ADMIN/STAFF/DOCTOR | Staff by user ID |
| PUT | `/api/staff/{id}` | ADMIN | Update staff |

### Health Records
| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/records/create` | DOCTOR | Create health record (completes appointment) |
| GET | `/api/records/patient/{id}` | DOCTOR/STAFF | View patient records |
| PUT | `/api/records/{id}` | DOCTOR | Update diagnosis/notes |

## Web App Pages

| Route | Page | Roles |
|---|---|---|
| `/login` | LoginPage | Public |
| `/admin/register` | AdminRegisterPage | ADMIN |
| `/dashboard` | DashboardHome | All authenticated |
| `/doctors` | DoctorListPage | All authenticated |
| `/patients` | ManagePatientsPage | ADMIN/STAFF/DOCTOR |
| `/doctors/manage` | ManageDoctorsPage | ADMIN/STAFF |
| `/staff` | ManageStaffPage | ADMIN |
| `/appointments` | AppointmentManagementPage | ADMIN/STAFF |
| `/schedules/new` | CreateSchedulePage | ADMIN/STAFF |
| `/my-queue` | DoctorAppointmentQueuePage | DOCTOR |
| `/records` | DoctorRecordsPage | DOCTOR |

## Mobile App Activities

| Activity | Purpose |
|---|---|
| LoginActivity | Login with auto-login if session exists |
| RegisterActivity | Patient registration with confirm-password validation |
| DoctorListActivity | Browse doctors, search by specialization |
| DoctorScheduleListActivity | View available slots, confirm booking |
| AppointmentHistoryActivity | View own appointment history |

## Setup Instructions

### Prerequisites
- Java 17+
- Node.js 18+
- Android Studio (latest stable)
- A Supabase PostgreSQL project

### 1. Backend

```bash
cd backend
# Set environment variables or edit src/main/resources/application.properties
#   DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET
./mvnw clean install
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` by default.

### 2. Web App

```bash
cd web
npm install
# Create .env with:
#   VITE_API_URL=http://localhost:8080/api
npm run dev
```

The app runs on `http://localhost:5173`.

### 3. Mobile App

Open `mobile/` in Android Studio. The Retrofit base URL defaults to `http://10.0.2.2:8080/` (emulator localhost). Build and run on an emulator or device with Android 8.0+.

## Current Status

All core features are implemented and integrated:

- **Backend:** Full REST API with 20+ endpoints, JWT auth, role-based access control, bean validation, DB-level queries with `@EntityGraph`, pessimistic locking for double-booking prevention
- **Web:** Complete dashboard with 11 routes, consistent teal/red design system, role-based navigation, error boundary, mounted-ref guard against StrictMode race conditions
- **Mobile:** 5 activities with full auth flow (auto-login, logout, confirm password), Retrofit + OkHttp with timeouts and header-level logging
- **Documentation:** SRS PDFs, UML diagrams (class, activity, sequence, use case, ERD), sprint traces, commit history summaries

### Not Yet Implemented
- OpenFDA API integration for medical classification
- SMTP email notifications via Mailtrap

## Diagrams & Docs

All documentation is in the `docs/` directory:
- `AMIHAN_MEDIBOOK_FINAL_SRS.pdf` — Final Software Requirements Specification
- `IT342_SRS_V1.0_Amihan_Gyle.pdf` — IT342 SRS version 1.0
- `Amihan_Gyle_MobileDevelopmentSetup.pdf` — Mobile dev environment setup
- `Amihan_Gyle_Phase2.pdf` — Phase 2 implementation
- `*.png` — Activity, Class, ERD, and Sequence diagrams
- `*.drawio` — Use case diagram source
- `commits-summary.md` — Full commit history with hashes
- `fixes-summary.md` — Audit fixes log
- `sprint-trace.md` — Architecture, endpoints, fix audit, current state
- `project-structure.md` — Complete directory tree of all 3 subprojects
