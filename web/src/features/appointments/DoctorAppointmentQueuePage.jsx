import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../auth/AuthContext';
import { getDoctorByUserId } from '../doctors/doctorService';
import { getDoctorAppointments } from './doctorAppointmentService';
import { createRecord } from '../records/recordService';

const STATUS_OPTIONS = ['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'];

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
      } catch (err) {
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

  const openRecord = (appt) => {
    setRecordFor(appt);
    setDiagnosis('');
    setNotes('');
    setRecordError('');
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
      await createRecord({
        appointmentId: recordFor.appointmentId,
        diagnosis,
        consultationNotes: notes,
      });
      setMessage(`Record saved for ${recordFor.patientName}.`);
      setRecordFor(null);
      fetchAppointments();
    } catch (err) {
      setRecordError(err.response?.data?.message || 'Failed to save record.');
    } finally {
      setSaving(false);
    }
  };

  const statusBadge = (status) => {
    const colors = {
      PENDING: 'bg-amber-50 text-amber-700 border-amber-200',
      CONFIRMED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
      COMPLETED: 'bg-blue-50 text-blue-700 border-blue-200',
      CANCELLED: 'bg-slate-100 text-slate-500 border-slate-200',
    };
    return `text-xs font-mono uppercase px-2 py-1 rounded border ${colors[status] || 'bg-slate-100 text-slate-500 border-slate-200'}`;
  };

  return (
    <div className="max-w-4xl">
      <h1 className="text-xl font-semibold text-[var(--color-ink)] mb-4">My Appointment Queue</h1>

      {message && <p className="text-sm text-emerald-600 font-medium mb-3">{message}</p>}

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

      {loading && <p className="text-sm text-[var(--color-ink-soft)]">Loading...</p>}
      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">{error}</p>
      )}
      {!loading && !error && appointments.length === 0 && (
        <p className="text-sm text-[var(--color-ink-soft)]">No appointments found.</p>
      )}

      {!loading && !error && appointments.length > 0 && (
        <div className="bg-white border border-[var(--color-border)] rounded-lg divide-y divide-[var(--color-border)]">
          {appointments.map((appt) => (
            <div key={appt.appointmentId} className="px-5 py-4 flex items-center justify-between">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{appt.patientName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">
                  {new Date(appt.startTime).toLocaleString()}
                </p>
              </div>
              <div className="flex items-center gap-3">
                <span className={statusBadge(appt.status)}>{appt.status}</span>
                {appt.status === 'CONFIRMED' && (
                  <button
                    onClick={() => openRecord(appt)}
                    className="text-sm text-[var(--color-panel-accent)] hover:text-[var(--color-panel)] font-medium transition"
                  >
                    Write Record
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {recordFor && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl w-full max-w-md p-5 shadow-xl">
            <h2 className="text-lg font-semibold text-[var(--color-ink)] mb-1">Consultation Record</h2>
            <p className="text-sm text-[var(--color-ink-soft)] mb-4">
              {recordFor.patientName} — {new Date(recordFor.startTime).toLocaleString()}
            </p>
            <form onSubmit={submitRecord} className="space-y-3">
              <div>
                <label className="block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5">
                  Diagnosis
                </label>
                <input
                  value={diagnosis}
                  onChange={(e) => setDiagnosis(e.target.value)}
                  className="w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition"
                  placeholder="e.g. Hypertension, Stage 1"
                />
              </div>
              <div>
                <label className="block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5">
                  Consultation Notes
                </label>
                <textarea
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  rows={4}
                  className="w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition"
                  placeholder="Observations, advice, follow-up..."
                />
              </div>
              {recordError && <p className="text-sm text-[var(--color-vital)] font-medium">{recordError}</p>}
              <div className="flex gap-2 pt-1">
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 bg-[var(--color-panel-accent)] hover:bg-[var(--color-panel)] disabled:opacity-40 text-white text-sm font-semibold py-2.5 rounded-lg transition"
                >
                  {saving ? 'Saving...' : 'Save & Complete'}
                </button>
                <button
                  type="button"
                  onClick={() => setRecordFor(null)}
                  className="px-4 text-sm text-[var(--color-ink-soft)] hover:text-[var(--color-ink)] font-medium transition"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorAppointmentQueuePage;
