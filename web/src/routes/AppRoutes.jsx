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

const DashboardHome = () => (
  <div className="text-slate-600 text-sm">Welcome to your MediBook dashboard.</div>
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