import { useState, useEffect, useRef } from 'react';
import { getAllAppointments, updateAppointmentStatus } from './appointmentService';

const selectClasses =
  'rounded-lg border border-[var(--color-border)] bg-white px-3 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

const AppointmentManagementPage = () => {
  const [appointments, setAppointments] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; };
  }, []);

  const fetchAppointments = async (filter = '') => {
    setLoading(true);
    setError('');
    try {
      const data = await getAllAppointments(filter || null);
      if (!mountedRef.current) return;
      setAppointments(data);
    } catch (err) {
      if (!mountedRef.current) return;
      setError(err.response?.data?.message || 'Failed to load appointments.');
    } finally {
      if (mountedRef.current) setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handleFilterChange = (e) => {
    const value = e.target.value;
    setStatusFilter(value);
    fetchAppointments(value);
  };

  const handleAction = async (appointmentId, newStatus) => {
    setActionError('');
    try {
      await updateAppointmentStatus(appointmentId, newStatus);
      fetchAppointments(statusFilter);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to update appointment.');
    }
  };

  const statusBadge = (status) => {
    const map = {
      PENDING: 'badge badge-pending',
      CONFIRMED: 'badge badge-confirmed',
      CANCELLED: 'badge badge-cancelled',
      COMPLETED: 'badge badge-completed',
    };
    return map[status] || 'badge badge-cancelled';
  };

  return (
    <div className="animate-fade-in-up">
      <div className="dashboard-header">
        <p className="dashboard-header-eyebrow">Scheduling</p>
        <h1 className="dashboard-header-title">Appointments</h1>
        <p className="dashboard-header-subtitle">Manage and update appointment statuses across the clinic.</p>
      </div>

      <div className="flex items-center justify-between mb-4">
        <div />
        <select value={statusFilter} onChange={handleFilterChange} className={selectClasses}>
          <option value="">All Statuses</option>
          <option value="PENDING">Pending</option>
          <option value="CONFIRMED">Confirmed</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="COMPLETED">Completed</option>
        </select>
      </div>

      {actionError && (
        <p className="text-sm text-[var(--color-vital)] font-medium mb-3">{actionError}</p>
      )}

      {loading && (
        <div className="flex items-center gap-2 text-sm text-[var(--color-ink-soft)]">
          <span className="spinner" /> Loading appointments...
        </div>
      )}

      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">{error}</p>
      )}

      {!loading && !error && appointments.length === 0 && (
        <div className="dashboard-card p-8 text-center">
          <p className="text-sm text-[var(--color-ink-soft)]">No appointments found.</p>
        </div>
      )}

      {!loading && !error && appointments.length > 0 && (
        <div className="dashboard-card divide-y divide-[var(--color-border)]">
          {appointments.map((appt) => (
            <div key={appt.appointmentId} className="px-5 py-4 flex items-center justify-between hover:bg-[var(--color-bg)] transition">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{appt.patientName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">
                  with {appt.doctorName} &mdash; {new Date(appt.startTime).toLocaleString()}
                </p>
              </div>

              <div className="flex items-center gap-3">
                <span className={statusBadge(appt.status)}>
                  <span className="badge-dot" />
                  {appt.status}
                </span>

                {appt.status === 'PENDING' && (
                  <>
                    <button
                      onClick={() => handleAction(appt.appointmentId, 'CONFIRMED')}
                      className="text-sm text-emerald-600 hover:text-emerald-700 font-medium transition"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() => handleAction(appt.appointmentId, 'CANCELLED')}
                      className="text-sm text-[var(--color-vital)] hover:text-[#ff5643] font-medium transition"
                    >
                      Cancel
                    </button>
                  </>
                )}

                {appt.status === 'CONFIRMED' && (
                  <button
                    onClick={() => handleAction(appt.appointmentId, 'COMPLETED')}
                    className="text-sm text-[var(--color-panel-accent)] hover:text-[var(--color-panel)] font-medium transition"
                  >
                    Mark Completed
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AppointmentManagementPage;
