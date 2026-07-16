import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const DashboardLayout = () => {
  const { user, logout } = useAuth();

  const navLink = (to, label) => (
    <Link
      to={to}
      className="font-mono text-[11px] tracking-[0.12em] uppercase text-white/70 hover:text-white transition"
    >
      {label}
    </Link>
  );

  return (
    <div className="min-h-screen flex flex-col" style={{ backgroundColor: 'var(--color-bg)' }}>
      <header className="bg-[var(--color-panel)] text-white">
        <div className="flex items-center justify-between px-6 lg:px-10 py-4">
          <div className="flex items-center gap-6">
            <Link to="/dashboard" className="flex items-center gap-2.5">
              <span className="w-2 h-2 rounded-full bg-[var(--color-vital)] pulse-dot" />
              <span className="font-display text-xl tracking-tight">MediBook</span>
            </Link>
            <nav className="hidden md:flex items-center gap-5">
              {navLink('/doctors', 'Doctors')}
              {navLink('/patients', 'Patients')}
              {(user?.role === 'ADMIN') && (
                navLink('/staff', 'Staff')
              )}
              {(user?.role === 'ADMIN' || user?.role === 'STAFF') && (
                <>
                  {navLink('/appointments', 'Appointments')}
                  {navLink('/schedules', 'Schedules')}
                </>
              )}
              {user?.role === 'DOCTOR' && (
                <>
                  {navLink('/my-queue', 'My Queue')}
                  {navLink('/records', 'Records')}
                </>
              )}
            </nav>
          </div>

          <div className="flex items-center gap-4">
            <span className="hidden sm:block text-sm text-white/70">
              {user?.fullName}
            </span>
            <span className="font-mono text-[10px] uppercase tracking-wider bg-white/15 text-white/80 px-2 py-0.5 rounded">
              {user?.role}
            </span>
            <button
              onClick={logout}
              className="font-mono text-[11px] uppercase tracking-[0.12em] text-[var(--color-vital)] hover:text-white transition"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="flex-1 px-6 lg:px-10 py-8">
        <Outlet />
      </main>
    </div>
  );
};

export default DashboardLayout;
