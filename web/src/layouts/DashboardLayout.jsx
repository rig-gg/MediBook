import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const DashboardLayout = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <span className="font-semibold text-slate-800">MediBook</span>
          <nav className="flex gap-4 text-sm">
            <Link to="/dashboard" className="text-slate-600 hover:text-slate-900">
              Dashboard
            </Link>
            <Link to="/doctors" className="text-slate-600 hover:text-slate-900">
              Doctors
            </Link>
          </nav>
        </div>

        <div className="flex items-center gap-4">
          <span className="text-sm text-slate-500">
            {user?.fullName} <span className="text-slate-400">({user?.role})</span>
          </span>
          <button
            onClick={logout}
            className="text-sm text-red-600 hover:text-red-700 font-medium"
          >
            Logout
          </button>
        </div>
      </header>

      <main className="p-6">
        <Outlet />
      </main>
    </div>
  );
};

export default DashboardLayout;