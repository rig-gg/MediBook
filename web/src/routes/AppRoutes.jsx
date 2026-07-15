import { useState, useEffect } from 'react';
import { Routes, Route, Navigate, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
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
import { getDoctors } from '../features/doctors/doctorService';
import { getPatients } from '../features/patients/patientService';
import { getAllAppointments } from '../features/appointments/appointmentService';
import { getDoctorByUserId } from '../features/doctors/doctorService';
import { getDoctorAppointments } from '../features/appointments/doctorAppointmentService';

const DashboardHome = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        if (user?.role === 'DOCTOR') {
          const doctor = await getDoctorByUserId(user.userId);
          const doctorId = doctor.doctorId;
          const appointments = await getDoctorAppointments(doctorId);
          setStats({
            myAppointments: appointments.length,
            pending: appointments.filter((a) => a.status === 'PENDING').length,
            confirmed: appointments.filter((a) => a.status === 'CONFIRMED').length,
            completed: appointments.filter((a) => a.status === 'COMPLETED').length,
          });
        } else {
          const [doctors, patients, appointments] = await Promise.all([
            getDoctors(),
            getPatients(),
            getAllAppointments(),
          ]);
          setStats({
            doctors: doctors.length,
            patients: patients.length,
            appointments: appointments.length,
            pending: appointments.filter((a) => a.status === 'PENDING').length,
          });
        }
      } catch {
        /* silent */
      }
    };
    load();
  }, [user?.userId, user?.role]);

  const roleLabel = {
    ADMIN: 'System Administrator',
    STAFF: 'Clinic Staff',
    DOCTOR: 'Healthcare Professional',
  };

  return (
    <div className="animate-fade-in-up space-y-6">
      <div className="dashboard-header">
        <p className="dashboard-header-eyebrow">Clinic Operations Portal</p>
        <h1 className="dashboard-header-title">Welcome back, {user?.fullName}.</h1>
        <p className="dashboard-header-subtitle">
          {roleLabel[user?.role] || user?.role} — manage appointments, doctors, patients, and schedules from one place.
        </p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {user?.role === 'DOCTOR' ? (
          <>
            <div className="stat-card animate-fade-in-up" style={{ animationDelay: '0ms' }}>
              <p className="stat-card-label">My Appointments</p>
              <p className="stat-card-value">{stats?.myAppointments ?? '\u2014'}</p>
            </div>
            <div className="stat-card animate-fade-in-up" style={{ animationDelay: '80ms' }}>
              <p className="stat-card-label">Pending</p>
              <p className="stat-card-value" style={stats?.pending > 0 ? { color: 'var(--color-vital)' } : {}}>
                {stats?.pending ?? '\u2014'}
              </p>
            </div>
            <div className="stat-card animate-fade-in-up" style={{ animationDelay: '160ms' }}>
              <p className="stat-card-label">Confirmed</p>
              <p className="stat-card-value">{stats?.confirmed ?? '\u2014'}</p>
            </div>
            <div className="stat-card animate-fade-in-up" style={{ animationDelay: '240ms' }}>
              <p className="stat-card-label">Completed</p>
              <p className="stat-card-value">{stats?.completed ?? '\u2014'}</p>
            </div>
          </>
        ) : (
          <>
            <div className="stat-card animate-fade-in-up" style={{ animationDelay: '0ms' }}>
              <p className="stat-card-label">Doctors</p>
              <p className="stat-card-value">{stats?.doctors ?? '\u2014'}</p>
            </div>
            <div className="stat-card animate-fade-in-up" style={{ animationDelay: '80ms' }}>
              <p className="stat-card-label">Patients</p>
              <p className="stat-card-value">{stats?.patients ?? '\u2014'}</p>
            </div>
            <div className="stat-card animate-fade-in-up" style={{ animationDelay: '160ms' }}>
              <p className="stat-card-label">Appointments</p>
              <p className="stat-card-value">{stats?.appointments ?? '\u2014'}</p>
            </div>
            <div className="stat-card animate-fade-in-up" style={{ animationDelay: '240ms' }}>
              <p className="stat-card-label">Pending</p>
              <p className="stat-card-value" style={stats?.pending > 0 ? { color: 'var(--color-vital)' } : {}}>
                {stats?.pending ?? '\u2014'}
              </p>
            </div>
          </>
        )}
      </div>

      {user?.role === 'ADMIN' && (
        <div className="dashboard-card p-5">
          <p className="dashboard-header-eyebrow">Quick Actions</p>
          <p className="text-sm text-[var(--color-ink-soft)]">
            Navigate to{' '}
            <Link to="/admin/register" className="text-[var(--color-panel-accent)] font-medium hover:underline">
              provision accounts
            </Link>
            ,{' '}
            <Link to="/staff" className="text-[var(--color-panel-accent)] font-medium hover:underline">
              manage staff
            </Link>
            , or view{' '}
            <Link to="/appointments" className="text-[var(--color-panel-accent)] font-medium hover:underline">
              all appointments
            </Link>
            .
          </p>
        </div>
      )}
    </div>
  );
};

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

      <Route path="*" element={user ? <Navigate to="/dashboard" replace /> : <Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes;