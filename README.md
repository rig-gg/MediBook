# MediBook: Health Appointment & Records Integration System

**Course:** IT342-[G01] Systems Integration and Architecture 1
**Developer:** Gyle M. Amihan

## Overview

MediBook is an integrated client-server healthcare ecosystem built to replace the manual appointment tracking and disconnected record-keeping still common in small healthcare facilities and local barangay clinics. It connects patients, clinic staff, doctors, and system administrators through a single platform, backed by a centralized REST API and cloud database.

The system is made up of three parts:

- **Backend API** — Spring Boot REST API handling authentication, appointments, records, and third-party integrations
- **Web Application** — ReactJS dashboard for clinic staff, doctors, and administrators
- **Mobile Application** — Android Kotlin app for patient self-service

This repository is the **monorepo root**, containing all three parts of the system in their own subdirectories:

```
medibook/
 ├── backend/     # Spring Boot REST API
 ├── web/         # ReactJS web dashboard
 └── mobile/      # Android Kotlin app (patient-facing)
```

The mobile app inside `mobile/` is what patients use to register, log in, view doctor availability, and book appointments.

## Problem It Solves

Small clinics often rely on manual logs or disconnected local software, which leads to scheduling conflicts, lost records, and no easy way for patients to book appointments without calling or visiting in person. MediBook centralizes this into one system so patients can book from their phones, staff can manage schedules from a dashboard, and doctors can keep consultation records tied directly to each appointment.

## Tech Stack

| Layer | Technology |
|---|---|
| Mobile App | Android (Kotlin) |
| Web App | ReactJS |
| Backend API | Spring Boot (Java) |
| Database | Supabase (PostgreSQL) |
| Authentication | JWT (JSON Web Tokens) + BCrypt password hashing |
| Networking (Mobile) | Retrofit |
| External Integrations | OpenFDA API (medical classification lookup), Supabase/Mailtrap SMTP (email notifications) |
| Deployment | Render (API), Netlify (Web), Supabase (DB) |

## User Roles

- **Patient** — registers and logs in through the mobile app, books appointments, views status
- **Clinic Staff** — manages incoming appointment requests, configures doctor schedules (web)
- **Doctor** — views daily queue, writes consultation records after completed appointments (web)
- **System Administrator** — provisions internal Staff/Doctor accounts (web); public registration is patient-only

## Mobile App Features

- **User Registration** — patient sign-up connected to the Spring Boot API, which persists the account to the Supabase database
- **User Login** — validates credentials against the backend, receives a JWT on success, and redirects to the patient dashboard
- **Role Restriction** — mobile registration only ever creates `PATIENT` accounts; staff and doctor accounts are provisioned separately by an admin through the web app

## Mobile App Structure

```
mobile/
 └── app/src/main/java/com/medibook/app/
      ├── ui/            # Activities/Fragments (Registration, Login, Dashboard, etc.)
      ├── network/        # Retrofit API service + client setup
      ├── model/           # Data classes (User, Patient, Appointment, etc.)
      └── util/            # Token storage, validation helpers
```

## Backend Connection

The mobile app communicates with the Spring Boot REST API over HTTPS:

- `POST /api/auth/register` — registers a new patient account
- `POST /api/auth/login` — authenticates a user and returns a JWT
- Additional endpoints for appointments and schedules are consumed once authentication is complete

The API itself connects to a Supabase PostgreSQL instance for persistence, with passwords hashed using BCrypt before being stored.

## Setup Instructions

MediBook is made up of three parts that need to run together: the **backend API**, the **web app**, and the **mobile app**. Set them up in that order, since both frontends depend on the backend being up and connected to the database.

### 1. Database (Supabase)

1. Create a free project at [supabase.com](https://supabase.com)
2. Once created, go to **Project Settings → Database** and copy the connection string (host, port, database name, user, password)
3. Run/import the schema so the `users`, `patients`, `doctors`, `doctor_schedules`, `appointments`, and `health_records` tables exist (see the ERD in the SRS)
4. Keep the Supabase project URL and API keys handy — you'll need them for the backend and for SMTP if using Supabase Auth's mail service

### 2. Backend API (Spring Boot)

**Requirements:** Java 17+, Maven (or the Maven wrapper), an IDE such as IntelliJ IDEA or VS Code

1. Clone this repository, then move into the backend folder:
   ```bash
   git clone <this-repo-url>
   cd medibook/backend
   ```
2. Open `src/main/resources/application.properties` (or `application.yml`) and set:
   ```properties
   spring.datasource.url=jdbc:postgresql://<your-supabase-host>:5432/postgres
   spring.datasource.username=<your-db-user>
   spring.datasource.password=<your-db-password>
   jwt.secret=<your-jwt-secret-key>
   openfda.api.base-url=https://api.fda.gov
   spring.mail.host=<smtp-host>
   spring.mail.username=<smtp-username>
   spring.mail.password=<smtp-password>
   ```
3. Build and run:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```
4. By default the API runs on `http://localhost:8080`. Confirm it's up by hitting a health/test endpoint (e.g. `GET /api/auth/ping` or similar)
5. When ready to deploy, push to **Render** (or your chosen host) and set the same environment variables there. Note the deployed base URL — it'll be needed by both the web and mobile apps

### 3. Web Application (ReactJS)

**Requirements:** Node.js 18+, npm or yarn

1. From the cloned repo, move into the web folder:
   ```bash
   cd medibook/web
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Create a `.env` file in the project root and point it at the backend API:
   ```env
   REACT_APP_API_BASE_URL=http://localhost:8080/api
   ```
   (swap in the deployed Render URL once the backend is live)
4. Run locally:
   ```bash
   npm start
   ```
5. The app should be available at `http://localhost:3000`. Log in using an ADMIN account (provisioned directly in the database, since admin accounts aren't self-registrable) to manage staff/doctor accounts and test the dashboard
6. To deploy, connect the repo to **Netlify** and set `REACT_APP_API_BASE_URL` as an environment variable in the Netlify site settings

### 4. Mobile Application (Android Kotlin)

**Requirements:** Android Studio (latest stable), Android SDK 26+ (Android 8.0+), a physical device or emulator

1. From the cloned repo, open the `mobile/` folder directly in **Android Studio** (File → Open → select `medibook/mobile`) and let Gradle sync
2. Locate the network/API config file (e.g. `network/ApiClient.kt` or `util/Constants.kt`) and set the base URL to your backend:
   ```kotlin
   const val BASE_URL = "http://10.0.2.2:8080/api/"   // 10.0.2.2 = localhost when using the Android emulator
   // const val BASE_URL = "https://your-app.onrender.com/api/"   // deployed backend
   ```
3. If testing against a locally running backend on a physical device, use your machine's local network IP instead of `10.0.2.2`, and make sure the device is on the same network
4. Build and run the app on an emulator or device running Android 8.0+
5. Test the flow end-to-end: register a new patient → confirm the record appears in the Supabase `patients`/`users` table → log in → confirm you land on the patient dashboard with a JWT stored for the session

### Running Everything Together (Local Development)

1. Start the backend first (`./mvnw spring-boot:run`) and confirm it's reachable
2. Start the web app (`npm start`) pointing at the local backend
3. Run the mobile app pointing at the local backend via `10.0.2.2` (emulator) or your LAN IP (physical device)
4. Register a patient from the mobile app, then verify the account shows up if you check the database directly or via the web admin panel

## Status

Currently in active development as part of the Systems Integration and Architecture 1 course project. This phase covers Android project setup and the patient Registration/Login flow, connected end-to-end to the Supabase-backed API.
