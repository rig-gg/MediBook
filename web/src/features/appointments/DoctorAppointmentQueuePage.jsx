import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../auth/AuthContext';
import { getDoctorByUserId } from '../doctors/doctorService';
import { getDoctorAppointments } from './doctorAppointmentService';
import { createRecord, getRecordByAppointment } from '../records/recordService';

const inputClasses =
  'w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

const labelClasses =
  'block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5';

const STATUS_OPTIONS = ['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'];

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

const DoctorAppointmentQueuePage = () => {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [doctorId, setDoctorId] = useState(null);

  const [recordFor, setRecordFor] = useState(null);
  const [diagnosis, setDiagnosis] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const [recordError, setRecordError] = useState('');
  const [message, setMessage] = useState('');
  const [fdaSuggestions, setFdaSuggestions] = useState([]);

  const [detailAppt, setDetailAppt] = useState(null);
  const [detailRecord, setDetailRecord] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; };
  }, []);

  useEffect(() => {
    const loadDoctor = async () => {
      try {
        const doc = await getDoctorByUserId(user.userId);
        if (mountedRef.current) setDoctorId(doc.doctorId);
      } catch {
        if (mountedRef.current) {
          setError('Failed to load doctor profile.');
          setLoading(false);
        }
      }
    };
    loadDoctor();
  }, [user.userId]);

  const fetchAppointments = async () => {
    if (!doctorId) return;
    setLoading(true);
    setError('');
    try {
      const data = statusFilter === 'ALL'
        ? await getDoctorAppointments(doctorId)
        : await getDoctorAppointments(doctorId, statusFilter);
      if (mountedRef.current) setAppointments(data);
    } catch (err) {
      if (mountedRef.current) setError(err.response?.data?.message || 'Failed to load appointments.');
    } finally {
      if (mountedRef.current) setLoading(false);
    }
  };

  useEffect(() => {
    if (doctorId) fetchAppointments();
  }, [doctorId, statusFilter]);

  const openDetail = async (appt) => {
    setDetailAppt(appt);
    setDetailRecord(null);
    setDetailLoading(true);
    try {
      const record = await getRecordByAppointment(appt.appointmentId);
      if (mountedRef.current) setDetailRecord(record);
    } catch {
      // 404 means no record yet — that's fine
    } finally {
      if (mountedRef.current) setDetailLoading(false);
    }
  };

  const openRecord = (appt) => {
    setDetailAppt(null);
    setRecordFor(appt);
    setDiagnosis('');
    setNotes('');
    setRecordError('');
    setFdaSuggestions([]);
  };

  const submitRecord = async (e) => {
    e.preventDefault();
    setRecordError('');
    if (!diagnosis.trim()) {
      setRecordError('Diagnosis is required.');
      return;
    }
    setSaving(true);
    try {
      const result = await createRecord({
        appointmentId: recordFor.appointmentId,
        diagnosis,
        consultationNotes: notes,
      });
      setMessage(`Record saved for ${recordFor.patientName}.`);
      setFdaSuggestions(result.fdaSuggestions || []);
      setRecordFor(null);
      fetchAppointments();
    } catch (err) {
      setRecordError(err.response?.data?.message || 'Failed to save record.');
    } finally {
      setSaving(false);
    }
  };

  const statusBadge = (status) => {
    const map = {
      PENDING: 'badge badge-pending',
      CONFIRMED: 'badge badge-confirmed',
      COMPLETED: 'badge badge-completed',
      CANCELLED: 'badge badge-cancelled',
    };
    return map[status] || 'badge badge-cancelled';
  };

  return (
    <div className="animate-fade-in-up">
      <div className="dashboard-header">
        <p className="dashboard-header-eyebrow">Doctor Workspace</p>
        <h1 className="dashboard-header-title">My Appointment Queue</h1>
        <p className="dashboard-header-subtitle">Click an appointment to view details. Write consultation records for confirmed visits.</p>
      </div>

      {message && <p className="text-sm text-emerald-600 font-medium mb-3 bg-emerald-50 border border-emerald-200 rounded-lg px-4 py-3">{message}</p>}

      {fdaSuggestions.length > 0 && (
        <div className="dashboard-card p-5 mb-4 animate-fade-in-up">
          <p className="font-mono text-[11px] uppercase tracking-wide text-[var(--color-panel-accent)] mb-3">
            FR-010 &middot; OpenFDA Drug Suggestions
          </p>
          <FdaSuggestions suggestions={fdaSuggestions} />
          <button onClick={() => setFdaSuggestions([])} className="mt-3 text-xs text-[var(--color-ink-soft)] hover:text-[var(--color-ink)] transition">
            Dismiss
          </button>
        </div>
      )}

      <div className="flex gap-2 mb-4 flex-wrap">
        {STATUS_OPTIONS.map((s) => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className={`text-sm px-3 py-1.5 rounded-lg font-medium transition ${
              statusFilter === s
                ? 'bg-[var(--color-panel)] text-white'
                : 'bg-white text-[var(--color-ink-soft)] border border-[var(--color-border)] hover:border-[var(--color-panel-accent)]'
            }`}
          >
            {s.charAt(0) + s.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      {loading && (
        <div className="flex items-center gap-2 text-sm text-[var(--color-ink-soft)]">
          <span className="spinner" /> Loading...
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
            <button
              key={appt.appointmentId}
              onClick={() => openDetail(appt)}
              className="w-full px-5 py-4 flex items-center justify-between hover:bg-[var(--color-bg)] transition text-left cursor-pointer"
            >
              <div>
                <p className="font-medium text-[var(--color-ink)]">{appt.patientName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">
                  {new Date(appt.startTime).toLocaleString()}
                </p>
              </div>
              <div className="flex items-center gap-3">
                <span className={statusBadge(appt.status)}>
                  <span className="badge-dot" />
                  {appt.status}
                </span>
                <span className="text-xs text-[var(--color-ink-soft)]">View &rarr;</span>
              </div>
            </button>
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

            {detailLoading && (
              <div className="flex items-center gap-2 text-sm text-[var(--color-ink-soft)] py-4">
                <span className="spinner" /> Loading health record...
              </div>
            )}

            {!detailLoading && detailRecord && (
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

            {!detailLoading && !detailRecord && detailAppt.status === 'CONFIRMED' && (
              <div className="border-t border-[var(--color-border)] pt-4">
                <p className="text-sm text-[var(--color-ink-soft)] mb-3">No health record yet.</p>
                <button
                  onClick={() => { setDetailAppt(null); openRecord(detailAppt); }}
                  className="btn-primary"
                >
                  Write Record
                </button>
              </div>
            )}

            {!detailLoading && !detailRecord && detailAppt.status !== 'CONFIRMED' && (
              <div className="border-t border-[var(--color-border)] pt-4">
                <p className="text-sm text-[var(--color-ink-soft)]">No health record for this appointment.</p>
              </div>
            )}
          </div>
        </div>
      )}

      {recordFor && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl w-full max-w-md p-5 shadow-xl animate-fade-in-up">
            <h2 className="text-lg font-semibold text-[var(--color-ink)] mb-1">Consultation Record</h2>
            <p className="text-sm text-[var(--color-ink-soft)] mb-4">
              {recordFor.patientName} &mdash; {new Date(recordFor.startTime).toLocaleString()}
            </p>
            <form onSubmit={submitRecord} className="space-y-3">
              <div>
                <label className={labelClasses}>Diagnosis</label>
                <input value={diagnosis} onChange={(e) => setDiagnosis(e.target.value)} className={inputClasses} placeholder="e.g. Hypertension, Stage 1" />
              </div>
              <div>
                <label className={labelClasses}>Consultation Notes</label>
                <textarea value={notes} onChange={(e) => setNotes(e.target.value)} rows={4} className={inputClasses} placeholder="Observations, advice, follow-up..." />
              </div>
              {recordError && <p className="text-sm text-[var(--color-vital)] font-medium">{recordError}</p>}
              <div className="flex gap-2 pt-1">
                <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Saving...' : 'Save & Complete'}</button>
                <button type="button" onClick={() => setRecordFor(null)} className="btn-ghost">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorAppointmentQueuePage;
