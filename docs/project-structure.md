# MediBook - Project Structure

> Generated on: 2026-07-15

## Overview

MediBook is a full-stack medical appointment booking system with three main components:

- **backend/** - Java Spring Boot REST API
- **web/** - React (Vite) frontend dashboard
- **mobile/** - Android (Kotlin) mobile app

---

## Root Level

```
MediBook/
├── .gitignore
├── README.md
├── backend/          # Spring Boot API
├── web/              # React Dashboard
├── mobile/           # Android App
└── .idea/            # IntelliJ IDEA config
```

---

## backend/

Spring Boot backend (Maven). Package root: `edu.cit.amihan.medibook`

```
backend/
├── .env
├── .env.example
├── .gitattributes
├── .gitignore
├── mvnw / mvnw.cmd
├── pom.xml
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── target/
│   └── classes/
│       └── application.properties
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── application.properties
    │   └── java/edu/cit/amihan/medibook/
    │       ├── Application.java
    │       │
    │       ├── auth/
    │       │   ├── AuthController.java
    │       │   └── dto/
    │       │       ├── AuthResponse.java
    │       │       ├── LoginRequest.java
    │       │       └── RegisterRequest.java
    │       │
    │       ├── user/
    │       │   ├── entity/
    │       │   │   ├── User.java
    │       │   │   └── Role.java
    │       │   ├── repository/
    │       │   │   └── UserRepository.java
    │       │   └── service/
    │       │       └── UserDetailsServiceImpl.java
    │       │
    │       ├── security/
    │       │   ├── SecurityConfig.java
    │       │   ├── JwtUtil.java
    │       │   └── JwtAuthFilter.java
    │       │
    │       ├── doctor/
    │       │   ├── controller/
    │       │   │   └── DoctorController.java
    │       │   ├── dto/
    │       │   │   ├── DoctorRequest.java
    │       │   │   └── DoctorResponse.java
    │       │   ├── entity/
    │       │   │   └── Doctor.java
    │       │   ├── repository/
    │       │   │   └── DoctorRepository.java
    │       │   └── service/
    │       │       └── DoctorService.java
    │       │
    │       ├── patient/
    │       │   ├── controller/
    │       │   │   └── PatientController.java
    │       │   ├── dto/
    │       │   │   └── PatientResponse.java
    │       │   ├── entity/
    │       │   │   └── Patient.java
    │       │   ├── repository/
    │       │   │   └── PatientRepository.java
    │       │   └── service/
    │       │       └── PatientService.java
    │       │
    │       ├── appointment/
    │       │   ├── controller/
    │       │   │   └── AppointmentController.java
    │       │   ├── dto/
    │       │   │   ├── AppointmentRequest.java
    │       │   │   └── AppointmentResponse.java
    │       │   ├── entity/
    │       │   │   ├── Appointment.java
    │       │   │   └── AppointmentStatus.java
    │       │   ├── repository/
    │       │   │   └── AppointmentRepository.java
    │       │   └── service/
    │       │       └── AppointmentService.java
    │       │
    │       ├── schedule/
    │       │   ├── controller/
    │       │   │   └── DoctorScheduleController.java
    │       │   ├── dto/
    │       │   │   ├── DoctorScheduleRequest.java
    │       │   │   └── DoctorScheduleResponse.java
    │       │   ├── entity/
    │       │   │   └── DoctorSchedule.java
    │       │   ├── repository/
    │       │   │   └── DoctorScheduleRepository.java
    │       │   └── service/
    │       │       └── DoctorScheduleService.java
    │       │
    │       ├── record/
    │       │   ├── controller/
    │       │   │   └── HealthRecordController.java
    │       │   ├── dto/
    │       │   │   ├── HealthRecordRequest.java
    │       │   │   └── HealthRecordResponse.java
    │       │   ├── entity/
    │       │   │   └── HealthRecord.java
    │       │   ├── repository/
    │       │   │   └── HealthRecordRepository.java
    │       │   └── service/
    │       │       └── HealthRecordService.java
    │       │
    │       ├── fda/
    │       │   ├── FdaConfig.java
    │       │   ├── FdaDrugSuggestion.java
    │       │   └── FdaService.java
    │       │
    │       ├── email/
    │       │   ├── EmailConfig.java
    │       │   └── EmailService.java
    │       │
    │       ├── clinicstaff/
    │       │   ├── controller/
    │       │   │   └── ClinicStaffController.java
    │       │   ├── dto/
    │       │   │   ├── ClinicStaffRequest.java
    │       │   │   └── ClinicStaffResponse.java
    │       │   ├── entity/
    │       │   │   └── ClinicStaff.java
    │       │   ├── repository/
    │       │   │   └── ClinicStaffRepository.java
    │       │   └── service/
    │       │       └── ClinicStaffService.java
    │       │
    │       └── common/
    │           └── exception/
    │               ├── GlobalExceptionHandler.java
    │               └── ResourceNotFoundException.java
    │
    └── test/
        └── java/edu/cit/amihan/medibook/
            └── ApplicationTests.java
```

### Backend Domain Modules

| Module | Description |
|--------|-------------|
| `auth` | Authentication & registration (login, register, JWT tokens) |
| `user` | User entity, roles, Spring Security user details |
| `security` | JWT filter, security config, token utilities |
| `doctor` | Doctor CRUD & management |
| `patient` | Patient CRUD & management |
| `appointment` | Appointment booking, status management, delete |
| `schedule` | Doctor schedule/availability management |
| `record` | Health record CRUD + get-by-appointment with FDA suggestions |
| `fda` | OpenFDA drug classification suggestions (FR-010) |
| `email` | SMTP email notifications via Mailtrap (FR-011) |
| `clinicstaff` | Clinic staff CRUD & management |
| `common` | Shared exceptions, global error handling |

---

## web/

React SPA built with Vite. Located at `web/src/`.

```
web/
├── .env
├── .gitignore
├── eslint.config.js
├── index.html
├── package.json
├── package-lock.json
├── README.md
├── vite.config.js
├── public/
│   └── vite.svg
├── dist/                     # Build output
│   ├── index.html
│   ├── vite.svg
│   └── assets/
│       ├── index-BXmr4SRw.css
│       └── index-DZdhFYQr.js
└── src/
    ├── main.jsx              # App entry point
    ├── App.jsx
    ├── App.css
    ├── index.css
    │
    ├── api/
    │   └── axiosInstance.js
    │
    ├── auth/
    │   └── AuthContext.jsx
    │
    ├── assets/
    │   └── react.svg
    │
    ├── components/
    │   ├── ErrorBoundary.jsx
    │   └── ProtectedRoute.jsx
    │
    ├── layouts/
    │   ├── AuthLayout.jsx
    │   └── DashboardLayout.jsx
    │
    ├── pages/
    │   ├── LoginPage.jsx
    │   └── AdminRegisterPage.jsx
    │
    ├── routes/
    │   └── AppRoutes.jsx
    │
    ├── services/
    │   └── authService.js
    │
    └── features/
        ├── doctors/
        │   ├── doctorService.js
        │   ├── DoctorListPage.jsx
        │   └── ManageDoctorsPage.jsx
        │
        ├── patients/
        │   ├── patientService.js
        │   └── ManagePatientsPage.jsx
        │
        ├── appointments/
        │   ├── appointmentService.js
        │   ├── doctorAppointmentService.js
        │   ├── AppointmentManagementPage.jsx
        │   └── DoctorAppointmentQueuePage.jsx
        │
        ├── schedules/
        │   ├── scheduleService.js
        │   └── CreateSchedulePage.jsx
        │
        ├── staff/
        │   ├── staffService.js
        │   └── ManageStaffPage.jsx
        │
        └── records/
            ├── recordService.js
            └── DoctorRecordsPage.jsx
```

### Web Feature Modules

| Feature | Files | Description |
|---------|-------|-------------|
| `doctors` | service + 2 pages | Doctor listing & management |
| `patients` | service + 1 page | Patient management |
| `appointments` | 2 services + 2 pages | Appointment management, doctor queue, detail modals, delete |
| `schedules` | service + 1 page | Doctor schedule creation |
| `staff` | service + 1 page | Staff listing & management |
| `records` | service + 1 page | Health records viewing |

---

## mobile/

Android app (Kotlin). Package root: `edu.cit.amihan.medibook`

```
mobile/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties
├── gradlew / gradlew.bat
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/edu/cit/amihan/medibook/
        │   │   ├── MedibookApplication.kt
        │   │   │
        │   │   ├── core/
        │   │   │   ├── network/
        │   │   │   │   ├── RetrofitClient.kt
        │   │   │   │   └── AuthInterceptor.kt
        │   │   │   └── utils/
        │   │   │       └── TokenManager.kt
        │   │   │
        │   │   └── feature/
        │   │       ├── auth/
        │   │       │   ├── model/
        │   │       │   │   ├── AuthResponse.kt
        │   │       │   │   ├── LoginRequest.kt
        │   │       │   │   └── RegisterRequest.kt
        │   │       │   ├── network/
        │   │       │   │   └── AuthApiService.kt
        │   │       │   └── ui/
        │   │       │       ├── login/
        │   │       │       │   └── LoginActivity.kt
        │   │       │       └── register/
        │   │       │           └── RegisterActivity.kt
        │   │       │
        │   │       ├── doctor/
        │   │       │   ├── model/
        │   │       │   │   └── Doctor.kt
        │   │       │   ├── network/
        │   │       │   │   └── DoctorApiService.kt
        │   │       │   └── ui/
        │   │       │       ├── DoctorListActivity.kt
        │   │       │       └── DoctorAdapter.kt
        │   │       │
        │   │       ├── schedule/
        │   │       │   ├── model/
        │   │       │   │   └── DoctorSchedule.kt
        │   │       │   ├── network/
        │   │       │   │   └── ScheduleApiService.kt
        │   │       │   └── ui/
        │   │       │       ├── DoctorScheduleListActivity.kt
        │   │       │       └── ScheduleAdapter.kt
        │   │       │
        │   │       └── appointment/
        │   │           ├── model/
        │   │           │   ├── AppointmentRequest.kt
        │   │           │   ├── AppointmentResponse.kt
        │   │           │   └── HealthRecordResponse.kt
        │   │           ├── network/
        │   │           │   ├── AppointmentApiService.kt
        │   │           │   └── RecordApiService.kt
        │   │           └── ui/
        │   │               ├── AppointmentHistoryActivity.kt
        │   │               └── AppointmentAdapter.kt
        │   │
        │   └── res/
        │       ├── layout/
        │       │   ├── activity_login.xml
        │       │   ├── activity_register.xml
        │       │   ├── activity_doctor_list.xml
        │       │   ├── activity_doctor_schedule_list.xml
        │       │   ├── activity_appointment_history.xml
        │       │   ├── dialog_appointment_detail.xml
        │       │   ├── item_doctor.xml
        │       │   ├── item_schedule.xml
        │       │   ├── item_appointment.xml
        │       │   └── item_fda_suggestion.xml
        │       ├── drawable/
        │       │   ├── ic_email.xml
        │       │   ├── ic_launcher_background.xml
        │       │   ├── ic_launcher_foreground.xml
        │       │   └── ic_person.xml
        │       ├── mipmap-anydpi/
        │       │   ├── ic_launcher.xml
        │       │   └── ic_launcher_round.xml
        │       ├── values/
        │       │   ├── colors.xml
        │       │   ├── strings.xml
        │       │   └── themes.xml
        │       ├── values-night/
        │       │   └── themes.xml
        │       └── xml/
        │           ├── backup_rules.xml
        │           ├── data_extraction_rules.xml
        │           └── network_security_config.xml
        │
        ├── androidTest/
        │   └── java/edu/cit/amihan/medibook/
        │       └── ExampleInstrumentedTest.kt
        │
        └── test/
            └── java/edu/cit/amihan/medibook/
                └── ExampleUnitTest.kt
```

### Mobile Feature Modules

| Feature | Model | Network | UI |
|---------|-------|---------|-----|
| `auth` | LoginRequest, RegisterRequest, AuthResponse | AuthApiService | LoginActivity, RegisterActivity |
| `doctor` | Doctor | DoctorApiService | DoctorListActivity, DoctorAdapter |
| `schedule` | DoctorSchedule | ScheduleApiService | DoctorScheduleListActivity, ScheduleAdapter |
| `appointment` | AppointmentRequest, AppointmentResponse, HealthRecordResponse, FdaDrugSuggestion | AppointmentApiService, RecordApiService | AppointmentHistoryActivity, AppointmentAdapter, detail dialog |

### Mobile Core

| Module | Description |
|--------|-------------|
| `RetrofitClient` | HTTP client singleton (authApi, doctorApi, scheduleApi, appointmentApi, recordApi) |
| `AuthInterceptor` | Attaches JWT token to requests |
| `TokenManager` | Local token storage |

---

## Tech Stack Summary

| Layer | Technology |
|-------|------------|
| **Backend** | Java, Spring Boot, Spring Security, JWT, WebClient, JavaMailSender, Maven |
| **Web** | React, Vite, React Router, Axios |
| **Mobile** | Kotlin, Android, Retrofit, Gradle (KTS) |
| **Base package** | `edu.cit.amihan.medibook` |

---

## API Endpoints (Controllers)

| Backend Controller | Responsibility |
|--------------------|----------------|
| `AuthController` | Login / Register |
| `DoctorController` | Doctor CRUD |
| `PatientController` | Patient CRUD |
| `AppointmentController` | Appointment booking, management, delete |
| `DoctorScheduleController` | Doctor schedule/availability |
| `HealthRecordController` | Patient health records (returns FDA drug suggestions on create and on get-by-appointment) |
| `ClinicStaffController` | Staff CRUD & search |
