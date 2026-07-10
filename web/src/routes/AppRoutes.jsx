import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import AdminRegisterPage from '../pages/AdminRegisterPage';
import ProtectedRoute from '../components/ProtectedRoute';
import DashboardLayout from '../layouts/DashboardLayout';
import DoctorListPage from '../features/doctors/DoctorListPage';
import ManageDoctorsPage from '../features/doctors/ManageDoctorsPage';
import ManagePatientsPage from '../features/patients/ManagePatientsPage';
import DoctorRecordsPage from '../features/records/DoctorRecordsPage';
import DoctorAppointmentQueuePage from '../features/appointments/DoctorAppointmentQueuePage';
import CreateSchedulePage from '../features/schedules/CreateSchedulePage';
import AppointmentManagementPage from '../features/appointments/AppointmentManagementPage';
import ManageStaffPage from '../features/staff/ManageStaffPage';

const DashboardHome = () => (
  <div className="max-w-2xl">
    <div className="rounded-xl border border-[var(--color-border)] bg-white p-8">
      <p className="font-mono text-xs tracking-[0.2em] uppercase text-[var(--color-panel-accent)] mb-2">
        Clinic Operations Portal
      </p>
      <h1 className="font-display text-3xl font-semibold text-[var(--color-ink)] mb-3">
        Welcome back.
      </h1>
      <p className="text-sm text-[var(--color-ink-soft)] leading-relaxed">
        Manage appointments, doctors, patients, and schedules — all from one place.
      </p>
    </div>
  </div>
);

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/admin/register"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminRegisterPage />
          </ProtectedRoute>
        }
      />

      <Route
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'STAFF', 'DOCTOR']}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardHome />} />
        <Route path="/doctors" element={<DoctorListPage />} />
        <Route path="/patients" element={<ManagePatientsPage />} />
        <Route
          path="/staff"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <ManageStaffPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/doctors/manage"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'STAFF']}>
              <ManageDoctorsPage />
            </ProtectedRoute>
          }
        />
        <Route path="/appointments" element={<AppointmentManagementPage />} />

        <Route
          path="/schedules/new"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'STAFF']}>
              <CreateSchedulePage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/records"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR']}>
              <DoctorRecordsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-queue"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR']}>
              <DoctorAppointmentQueuePage />
            </ProtectedRoute>
          }
        />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes;