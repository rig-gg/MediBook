import { useState, useEffect, useRef } from 'react';
import { getAllAppointments, updateAppointmentStatus } from './appointmentService';

const statusStyles = {
  PENDING: 'bg-amber-50 text-amber-700 border-amber-200',
  CONFIRMED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  CANCELLED: 'bg-slate-100 text-slate-500 border-slate-200',
  COMPLETED: 'bg-blue-50 text-blue-700 border-blue-200',
};

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

  return (
    <div className="max-w-4xl">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-semibold text-[var(--color-ink)]">Appointments</h1>

        <select
          value={statusFilter}
          onChange={handleFilterChange}
          className="rounded-lg border border-[var(--color-border)] bg-white px-3 py-2 text-sm"
        >
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

      {loading && <p className="text-sm text-[var(--color-ink-soft)]">Loading appointments...</p>}

      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-md px-4 py-3">
          {error}
        </p>
      )}

      {!loading && !error && appointments.length === 0 && (
        <p className="text-sm text-[var(--color-ink-soft)]">No appointments found.</p>
      )}

      {!loading && !error && appointments.length > 0 && (
        <div className="bg-white border border-[var(--color-border)] rounded-lg divide-y divide-[var(--color-border)]">
          {appointments.map((appt) => (
            <div key={appt.appointmentId} className="px-4 py-3 flex items-center justify-between">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{appt.patientName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">
                  with Dr. {appt.doctorName} — {new Date(appt.startTime).toLocaleString()}
                </p>
              </div>

              <div className="flex items-center gap-3">
                <span
                  className={`text-xs font-mono uppercase px-2 py-1 rounded border ${statusStyles[appt.status]}`}
                >
                  {appt.status}
                </span>

                {appt.status === 'PENDING' && (
                  <>
                    <button
                      onClick={() => handleAction(appt.appointmentId, 'CONFIRMED')}
                      className="text-sm text-emerald-600 hover:text-emerald-700 font-medium"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() => handleAction(appt.appointmentId, 'CANCELLED')}
                      className="text-sm text-red-600 hover:text-red-700 font-medium"
                    >
                      Cancel
                    </button>
                  </>
                )}

                {appt.status === 'CONFIRMED' && (
                  <button
                    onClick={() => handleAction(appt.appointmentId, 'COMPLETED')}
                    className="text-sm text-blue-600 hover:text-blue-700 font-medium"
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