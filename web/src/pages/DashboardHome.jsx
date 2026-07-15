import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { getDoctors, getDoctorByUserId } from '../features/doctors/doctorService';
import { getPatients } from '../features/patients/patientService';
import { getAllAppointments } from '../features/appointments/appointmentService';
import { getDoctorAppointments } from '../features/appointments/doctorAppointmentService';
import { getSchedules } from '../features/schedules/scheduleService';

const statusBadge = (status) => {
  const map = {
    PENDING: 'badge badge-pending',
    CONFIRMED: 'badge badge-confirmed',
    CANCELLED: 'badge badge-cancelled',
    COMPLETED: 'badge badge-completed',
  };
  return map[status] || 'badge badge-cancelled';
};

const isSameDay = (isoString, ref) => {
  const d = new Date(isoString);
  return (
    d.getFullYear() === ref.getFullYear() &&
    d.getMonth() === ref.getMonth() &&
    d.getDate() === ref.getDate()
  );
};

const DashboardHome = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [todaySchedules, setTodaySchedules] = useState([]);

  useEffect(() => {
    const load = async () => {
      try {
        if (user?.role === 'DOCTOR') {
          const doctor = await getDoctorByUserId(user.userId);
          const doctorId = doctor.doctorId;
          const [appts, schedules] = await Promise.all([
            getDoctorAppointments(doctorId),
            getSchedules(doctorId),
          ]);

          setAppointments(appts);
          setStats({
            myAppointments: appts.length,
            pending: appts.filter((a) => a.status === 'PENDING').length,
            confirmed: appts.filter((a) => a.status === 'CONFIRMED').length,
            completed: appts.filter((a) => a.status === 'COMPLETED').length,
          });

          const today = new Date();
          setTodaySchedules(schedules.filter((s) => isSameDay(s.startTime, today)));
        } else {
          const [doctors, patients, appts, schedules] = await Promise.all([
            getDoctors(),
            getPatients(),
            getAllAppointments(),
            getSchedules(),
          ]);

          setAppointments(appts);
          setStats({
            doctors: doctors.length,
            patients: patients.length,
            appointments: appts.length,
            pending: appts.filter((a) => a.status === 'PENDING').length,
          });

          const today = new Date();
          setTodaySchedules(schedules.filter((s) => isSameDay(s.startTime, today)));
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

  const pendingAppointments = appointments
    .filter((a) => a.status === 'PENDING')
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
    .slice(0, 5);

  const upcomingAppointments = appointments
    .filter((a) => a.status === 'CONFIRMED' && new Date(a.startTime) >= new Date())
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
    .slice(0, 5);

  const appointmentsLink = user?.role === 'DOCTOR' ? '/my-queue' : '/appointments';

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

      <div className="dashboard-card p-5">
        <p className="dashboard-header-eyebrow mb-2">Quick Actions</p>
        <div className="flex flex-wrap gap-2">
          {user?.role === 'ADMIN' && (
            <>
              <Link to="/admin/register" className="btn-outline">Provision Account</Link>
              <Link to="/staff" className="btn-outline">Manage Staff</Link>
            </>
          )}
          {(user?.role === 'ADMIN' || user?.role === 'STAFF') && (
            <>
              <Link to="/doctors/manage" className="btn-outline">Manage Doctors</Link>
              <Link to="/schedules/new" className="btn-outline">Add Schedule</Link>
              <Link to="/appointments" className="btn-outline">All Appointments</Link>
            </>
          )}
          {user?.role === 'DOCTOR' && (
            <>
              <Link to="/my-queue" className="btn-outline">My Queue</Link>
              <Link to="/records" className="btn-outline">Records</Link>
            </>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="dashboard-card p-5">
          <div className="flex items-center justify-between mb-3">
            <p className="dashboard-header-eyebrow">Pending Appointments</p>
            {pendingAppointments.length > 0 && (
              <Link to={appointmentsLink} className="text-xs text-[var(--color-panel-accent)] hover:underline">
                View all
              </Link>
            )}
          </div>
          {pendingAppointments.length === 0 ? (
            <p className="text-sm text-[var(--color-ink-soft)]">No pending appointments.</p>
          ) : (
            <div className="space-y-3">
              {pendingAppointments.map((a) => (
                <div key={a.appointmentId} className="flex items-center justify-between">
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-[var(--color-ink)] truncate">
                      {user?.role === 'DOCTOR' ? a.patientName : `${a.patientName} · ${a.doctorName}`}
                    </p>
                    <p className="text-xs text-[var(--color-ink-soft)]">
                      {new Date(a.startTime).toLocaleString()}
                    </p>
                  </div>
                  <span className={statusBadge(a.status)}>
                    <span className="badge-dot" />
                    {a.status}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="dashboard-card p-5">
          <div className="flex items-center justify-between mb-3">
            <p className="dashboard-header-eyebrow">Upcoming Appointments</p>
            {upcomingAppointments.length > 0 && (
              <Link to={appointmentsLink} className="text-xs text-[var(--color-panel-accent)] hover:underline">
                View all
              </Link>
            )}
          </div>
          {upcomingAppointments.length === 0 ? (
            <p className="text-sm text-[var(--color-ink-soft)]">No upcoming appointments.</p>
          ) : (
            <div className="space-y-3">
              {upcomingAppointments.map((a) => (
                <div key={a.appointmentId} className="flex items-center justify-between">
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-[var(--color-ink)] truncate">
                      {user?.role === 'DOCTOR' ? a.patientName : `${a.patientName} · ${a.doctorName}`}
                    </p>
                    <p className="text-xs text-[var(--color-ink-soft)]">
                      {new Date(a.startTime).toLocaleString()}
                    </p>
                  </div>
                  <span className={statusBadge(a.status)}>
                    <span className="badge-dot" />
                    {a.status}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="dashboard-card p-5">
        <p className="dashboard-header-eyebrow mb-3">Today's Schedule</p>
        {todaySchedules.length === 0 ? (
          <p className="text-sm text-[var(--color-ink-soft)]">No schedule entries for today.</p>
        ) : (
          <div className="divide-y divide-[var(--color-border)]">
            {todaySchedules
              .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
              .map((s) => (
                <div key={s.scheduleId} className="py-2.5 flex items-center justify-between">
                  <div className="min-w-0">
                    {user?.role !== 'DOCTOR' && (
                      <p className="text-sm font-medium text-[var(--color-ink)] truncate">
                        {s.doctorName} <span className="text-[var(--color-ink-soft)] font-normal">— {s.specialization}</span>
                      </p>
                    )}
                    <p className="text-xs text-[var(--color-ink-soft)]">
                      {new Date(s.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      {' – '}
                      {new Date(s.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </p>
                  </div>
                  <span
                    className={`font-mono text-[10px] uppercase tracking-wider px-2 py-0.5 rounded ${
                      s.isAvailable
                        ? 'bg-emerald-50 text-emerald-700'
                        : 'bg-[var(--color-bg)] text-[var(--color-ink-soft)]'
                    }`}
                  >
                    {s.isAvailable ? 'Available' : 'Booked'}
                  </span>
                </div>
              ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardHome;
