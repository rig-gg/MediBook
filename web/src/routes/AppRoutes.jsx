import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import AdminRegisterPage from '../pages/AdminRegisterPage';
import ProtectedRoute from '../components/ProtectedRoute';

// Simple placeholder so DOCTOR/STAFF have somewhere to land after login
const DashboardPlaceholder = () => (
  <div className="min-h-screen flex items-center justify-center bg-slate-100">
    <p className="text-slate-600 text-sm">Dashboard coming soon.</p>
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
        path="/dashboard"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'STAFF', 'DOCTOR']}>
            <DashboardPlaceholder />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes;