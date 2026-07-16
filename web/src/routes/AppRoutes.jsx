import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import LoginPage from '../pages/LoginPage';
import DashboardHome from '../pages/DashboardHome';
import AdminRegisterPage from '../pages/AdminRegisterPage';
import ProtectedRoute from '../components/ProtectedRoute';
import DashboardLayout from '../layouts/DashboardLayout';
import DoctorListPage from '../features/doctors/DoctorListPage';
import ManagePatientsPage from '../features/patients/ManagePatientsPage';
import DoctorRecordsPage from '../features/records/DoctorRecordsPage';
import DoctorAppointmentQueuePage from '../features/appointments/DoctorAppointmentQueuePage';
import CreateSchedulePage from '../features/schedules/CreateSchedulePage';
import ManageSchedulesPage from '../features/schedules/ManageSchedulesPage';
import AppointmentManagementPage from '../features/appointments/AppointmentManagementPage';
import ManageStaffPage from '../features/staff/ManageStaffPage';

const AppRoutes = () => {
  const { user, loading } = useAuth();

  if (loading) return null;

  return (
    <Routes>
      <Route path="/" element={user ? <Navigate to="/dashboard" replace /> : <Navigate to="/login" replace />} />
      <Route path="/login" element={user ? <Navigate to="/dashboard" replace /> : <LoginPage />} />

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
        <Route path="/appointments" element={<AppointmentManagementPage />} />

        <Route
          path="/schedules"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'STAFF']}>
              <ManageSchedulesPage />
            </ProtectedRoute>
          }
        />

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

      <Route path="*" element={user ? <Navigate to="/dashboard" replace /> : <Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes;
