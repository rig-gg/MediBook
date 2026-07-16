import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../auth/AuthContext';
import { getAllAppointments, updateAppointmentStatus, deleteAppointment } from './appointmentService';
import { getRecordByAppointment } from '../records/recordService';
import Toast from '../../components/Toast';

const selectClasses =
  'rounded-lg border border-[var(--color-border)] bg-white px-3 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

const FdaSuggestions = ({ suggestions }) => {
  if (!suggestions || suggestions.length === 0) return null;
  return (
    <div className="mt-3">
      <p className="font-mono text-[11px] uppercase tracking-wide text-[var(--color-panel-accent)] mb-2">
        OpenFDA Drug Suggestions
      </p>
      <div className="space-y-2">
        {suggestions.map((drug, idx) => (
          <div key={idx} className="p-3 rounded-lg bg-[var(--color-bg)] border border-[var(--color-border)]">
            <p className="font-medium text-sm text-[var(--color-ink)]">
              {drug.brandName || drug.genericName || 'Unknown Drug'}
            </p>
            {drug.brandName && drug.genericName && (
              <p className="text-xs text-[var(--color-ink-soft)] mt-0.5">Generic: {drug.genericName}</p>
            )}
            {drug.route && <p className="text-xs text-[var(--color-ink-soft)]">Route: {drug.route}</p>}
            {drug.indication && (
              <p className="text-xs text-[var(--color-ink-soft)] mt-1 line-clamp-2">{drug.indication}</p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

const AppointmentManagementPage = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [appointments, setAppointments] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [toast, setToast] = useState(null);

  const [detailAppt, setDetailAppt] = useState(null);
  const [detailRecord, setDetailRecord] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

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
    try {
      await updateAppointmentStatus(appointmentId, newStatus);
      setAppointments((prev) =>
        prev.map((a) => (a.appointmentId === appointmentId ? { ...a, status: newStatus } : a))
      );
      const statusLabel = { CONFIRMED: 'approved', CANCELLED: 'cancelled', COMPLETED: 'completed' };
      setToast({
        message: `Appointment ${statusLabel[newStatus] || newStatus.toLowerCase()}. Patient notified via email.`,
        type: 'success',
      });
    } catch (err) {
      setToast({ message: err.response?.data?.message || 'Failed to update appointment.', type: 'error' });
    }
  };

  const handleDelete = async (appointmentId) => {
    try {
      await deleteAppointment(appointmentId);
      setAppointments((prev) => prev.filter((a) => a.appointmentId !== appointmentId));
      setDetailAppt(null);
      setToast({ message: 'Appointment deleted.', type: 'success' });
    } catch (err) {
      setToast({ message: err.response?.data?.message || 'Failed to delete appointment.', type: 'error' });
    }
  };

  const isPast = (appt) => new Date(appt.startTime) < new Date();

  const openDetail = async (appt) => {
    setDetailAppt(appt);
    setDetailRecord(null);
    if (isAdmin) return;
    setDetailLoading(true);
    try {
      const record = await getRecordByAppointment(appt.appointmentId);
      if (mountedRef.current) setDetailRecord(record);
    } catch {
      // no record yet
    } finally {
      if (mountedRef.current) setDetailLoading(false);
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
        <p className="dashboard-header-eyebrow">Scheduling &middot; FR-011 Email Notifications</p>
        <h1 className="dashboard-header-title">Appointments</h1>
        <p className="dashboard-header-subtitle">Click an appointment to view details. Patients receive email notifications on status changes.</p>
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
              <button
                onClick={() => openDetail(appt)}
                className="flex-1 text-left min-w-0 cursor-pointer"
              >
                <p className="font-medium text-[var(--color-ink)]">{appt.patientName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">
                  with {appt.doctorName} &mdash; {new Date(appt.startTime).toLocaleString()}
                </p>
              </button>

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

                {(appt.status === 'CANCELLED' || (appt.status === 'CONFIRMED' && isPast(appt))) && (
                  <button
                    onClick={() => handleDelete(appt.appointmentId)}
                    className="text-sm text-[var(--color-vital)] hover:text-[#ff5643] font-medium transition"
                  >
                    Delete
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {detailAppt && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl w-full max-w-lg max-h-[85vh] overflow-y-auto p-5 shadow-xl animate-fade-in-up">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-[var(--color-ink)]">Appointment Details</h2>
              <button onClick={() => setDetailAppt(null)} className="text-[var(--color-ink-soft)] hover:text-[var(--color-ink)] text-sm">&times; Close</button>
            </div>

            <div className="space-y-3 mb-4">
              <div className="flex justify-between text-sm">
                <span className="text-[var(--color-ink-soft)]">Patient</span>
                <span className="font-medium text-[var(--color-ink)]">{detailAppt.patientName}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-[var(--color-ink-soft)]">Doctor</span>
                <span className="font-medium text-[var(--color-ink)]">{detailAppt.doctorName}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-[var(--color-ink-soft)]">Scheduled</span>
                <span className="font-medium text-[var(--color-ink)]">{new Date(detailAppt.startTime).toLocaleString()}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-[var(--color-ink-soft)]">Status</span>
                <span className={statusBadge(detailAppt.status)}>
                  <span className="badge-dot" />
                  {detailAppt.status}
                </span>
              </div>
            </div>

            {!isAdmin && detailLoading && (
              <div className="flex items-center gap-2 text-sm text-[var(--color-ink-soft)] py-4">
                <span className="spinner" /> Loading health record...
              </div>
            )}

            {!isAdmin && !detailLoading && detailRecord && (
              <div className="border-t border-[var(--color-border)] pt-4">
                <p className="font-mono text-[11px] uppercase tracking-wide text-[var(--color-panel-accent)] mb-2">
                  Health Record
                </p>
                <div className="bg-[var(--color-bg)] rounded-lg p-4 space-y-2">
                  <div>
                    <p className="text-xs text-[var(--color-ink-soft)] uppercase">Diagnosis</p>
                    <p className="text-sm font-medium text-[var(--color-ink)]">{detailRecord.diagnosis}</p>
                  </div>
                  {detailRecord.consultationNotes && (
                    <div>
                      <p className="text-xs text-[var(--color-ink-soft)] uppercase">Notes</p>
                      <p className="text-sm text-[var(--color-ink)]">{detailRecord.consultationNotes}</p>
                    </div>
                  )}
                  <div>
                    <p className="text-xs text-[var(--color-ink-soft)]">Recorded at {new Date(detailRecord.recordedAt).toLocaleString()}</p>
                  </div>
                </div>
                <FdaSuggestions suggestions={detailRecord.fdaSuggestions} />
              </div>
            )}

            {!isAdmin && !detailLoading && !detailRecord && (
              <div className="border-t border-[var(--color-border)] pt-4">
                <p className="text-sm text-[var(--color-ink-soft)]">No health record for this appointment.</p>
              </div>
            )}

            <div className="flex justify-end gap-2 mt-5">
              {detailAppt.status === 'PENDING' && (
                <>
                  <button
                    onClick={() => { setDetailAppt(null); handleAction(detailAppt.appointmentId, 'CONFIRMED'); }}
                    className="btn-primary bg-emerald-600 hover:bg-emerald-700"
                  >
                    Approve
                  </button>
                  <button
                    onClick={() => { setDetailAppt(null); handleAction(detailAppt.appointmentId, 'CANCELLED'); }}
                    className="btn-primary bg-[var(--color-vital)] hover:bg-[#ff5643]"
                  >
                    Cancel
                  </button>
                </>
              )}
              {detailAppt.status === 'CONFIRMED' && (
                <button
                  onClick={() => { setDetailAppt(null); handleAction(detailAppt.appointmentId, 'COMPLETED'); }}
                  className="btn-primary"
                >
                  Mark Completed
                </button>
              )}
              {(detailAppt.status === 'CANCELLED' || (detailAppt.status === 'CONFIRMED' && isPast(detailAppt))) && (
                <button
                  onClick={() => handleDelete(detailAppt.appointmentId)}
                  className="btn-primary bg-[var(--color-vital)] hover:bg-[#ff5643]"
                >
                  Delete
                </button>
              )}
            </div>
          </div>
        </div>
      )}
      {toast && <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />}
    </div>
  );
};

export default AppointmentManagementPage;
